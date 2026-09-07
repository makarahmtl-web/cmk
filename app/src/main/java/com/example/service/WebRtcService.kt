package com.example.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.webrtc.*

enum class CallState {
    IDLE, DIALING, CONNECTING, ACTIVE, DISCONNECTED
}

class WebRtcService(private val context: Context) {
    private val TAG = "WebRtcService"

    // Call state variables
    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isCameraOn = MutableStateFlow(true)
    val isCameraOn: StateFlow<Boolean> = _isCameraOn.asStateFlow()

    private val _currentCallPartner = MutableStateFlow("")
    val currentCallPartner: StateFlow<String> = _currentCallPartner.asStateFlow()

    private val _isVideoCall = MutableStateFlow(true)
    val isVideoCall: StateFlow<Boolean> = _isVideoCall.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    // WebRTC SDK core components
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var eglBase: EglBase? = null

    // For rendering streams
    var localSurfaceView: SurfaceViewRenderer? = null
    var remoteSurfaceView: SurfaceViewRenderer? = null

    private var callTimer: java.util.Timer? = null

    private var isInitialized = false

    init {
        // Defer WebRTC initialization to avoid startup crashes on devices with restricted hardware/permissions
    }

    private fun ensureWebRtcInitialized() {
        if (isInitialized) return
        try {
            // Create EglBase for video hardware acceleration
            if (eglBase == null) {
                eglBase = EglBase.create()
            }

            // Configure PeerConnectionFactory options safely without native tracer
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)

            val factoryBuilder = PeerConnectionFactory.builder()
                .setOptions(PeerConnectionFactory.Options())

            // Hardware video encoder/decoder initialization with EGL context
            eglBase?.let { egl ->
                try {
                    factoryBuilder.setVideoEncoderFactory(DefaultVideoEncoderFactory(egl.eglBaseContext, true, true))
                    factoryBuilder.setVideoDecoderFactory(DefaultVideoDecoderFactory(egl.eglBaseContext))
                } catch (t: Throwable) {
                    Log.w(TAG, "Hardware video encoder fallback: ${t.message}")
                }
            }

            peerConnectionFactory = factoryBuilder.createPeerConnectionFactory()
            isInitialized = true
            Log.d(TAG, "PeerConnectionFactory initialized successfully.")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize WebRTC engine safely", t)
        }
    }

    fun initVideoRenderers(localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        ensureWebRtcInitialized()
        val eglContext = eglBase?.eglBaseContext ?: return
        localSurfaceView = localView
        remoteSurfaceView = remoteView

        try {
            localView.init(eglContext, null)
            localView.setMirror(true)
            localView.setEnableHardwareScaler(true)

            remoteView.init(eglContext, null)
            remoteView.setEnableHardwareScaler(true)
            Log.d(TAG, "Video renderers initialized.")
        } catch (t: Throwable) {
            Log.w(TAG, "initVideoRenderers exception: ${t.message}")
        }
    }

    fun startCall(isVideo: Boolean, partnerName: String) {
        ensureWebRtcInitialized()
        _isVideoCall.value = isVideo
        _currentCallPartner.value = partnerName
        _callState.value = CallState.DIALING
        _isMuted.value = false
        _isSpeakerOn.value = true
        _isCameraOn.value = isVideo
        _callDurationSeconds.value = 0

        Log.d(TAG, "Initiating call to $partnerName, VideoEnabled: $isVideo")

        // In production, we request/setup local tracks and peer connections
        try {
            setupLocalMediaStream(isVideo)
            createPeerConnection()
        } catch (e: Exception) {
            Log.e(TAG, "Failed during local media setup or peer connection creation", e)
        }

        // Simulate connecting and moving to active state
        kotlinx.coroutines.MainScope().launch {
            kotlinx.coroutines.delay(2000)
            _callState.value = CallState.CONNECTING
            kotlinx.coroutines.delay(1500)
            _callState.value = CallState.ACTIVE
            startTimer()
        }
    }

    private fun setupLocalMediaStream(isVideo: Boolean) {
        val factory = peerConnectionFactory ?: return

        // 1. Setup Audio Track
        val audioConstraints = MediaConstraints()
        localAudioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack("local_audio_track_id", localAudioSource)
        localAudioTrack?.setEnabled(true)

        // 2. Setup Video Track (if isVideo is true)
        if (isVideo) {
            videoCapturer = createCameraCapturer()
            localVideoSource = factory.createVideoSource(false)
            
            val capturer = videoCapturer
            if (capturer != null) {
                // Initialize capturer
                capturer.initialize(
                    SurfaceTextureHelper.create("camera_capture_thread", eglBase?.eglBaseContext),
                    context,
                    localVideoSource?.capturerObserver
                )
                // Start capturing (640x480 at 30fps)
                capturer.startCapture(640, 480, 30)

                localVideoTrack = factory.createVideoTrack("local_video_track_id", localVideoSource)
                localVideoTrack?.setEnabled(true)

                // Render local video stream
                localSurfaceView?.let { localView ->
                    localVideoTrack?.addSink(localView)
                }
            }
        }
    }

    private fun createPeerConnection() {
        val factory = peerConnectionFactory ?: return
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "ICE Connection Change: $state")
                if (state == PeerConnection.IceConnectionState.DISCONNECTED) {
                    _callState.value = CallState.DISCONNECTED
                    endCall()
                }
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate?) {
                Log.d(TAG, "ICE Candidate Gathered: ${candidate?.sdp}")
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {
                Log.d(TAG, "Remote media stream added.")
                if (stream != null && stream.videoTracks.isNotEmpty()) {
                    val remoteVideoTrack = stream.videoTracks[0]
                    remoteVideoTrack.setEnabled(true)
                    remoteSurfaceView?.let { remoteView ->
                        remoteVideoTrack.addSink(remoteView)
                    }
                }
            }
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {}
        })

        // Add local tracks to peer connection
        localAudioTrack?.let { peerConnection?.addTrack(it, listOf("media_stream_id")) }
        localVideoTrack?.let { peerConnection?.addTrack(it, listOf("media_stream_id")) }
    }

    private fun createCameraCapturer(): VideoCapturer? {
        val enumerator = if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator(true)
        }

        val deviceNames = enumerator.deviceNames
        // Select front facing camera first
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) return capturer
            }
        }

        // Fallback to back camera
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) return capturer
            }
        }
        return null
    }

    fun endCall() {
        Log.d(TAG, "Ending current active call.")
        _callState.value = CallState.DISCONNECTED
        stopTimer()

        // Clean up peer connections and tracks
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null

            localVideoSource?.dispose()
            localVideoSource = null

            localAudioSource?.dispose()
            localAudioSource = null

            peerConnection?.close()
            peerConnection = null

            // Release renderers safely
            localSurfaceView?.release()
            remoteSurfaceView?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Exception releasing WebRTC calling hardware", e)
        }

        _callState.value = CallState.IDLE
    }

    fun toggleMute() {
        val nextState = !_isMuted.value
        _isMuted.value = nextState
        localAudioTrack?.setEnabled(!nextState)
        Log.d(TAG, "Mute toggled: $nextState")
    }

    fun toggleSpeaker() {
        val nextState = !_isSpeakerOn.value
        _isSpeakerOn.value = nextState
        // In fully wired hardware, we interact with AudioManager
        Log.d(TAG, "Speaker toggled: $nextState")
    }

    fun toggleCameraFlip() {
        val capturer = videoCapturer as? CameraVideoCapturer
        if (capturer != null) {
            capturer.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
                override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                    _isCameraOn.value = true
                    Log.d(TAG, "Camera switched successfully. Front facing: $isFrontCamera")
                }
                override fun onCameraSwitchError(error: String?) {
                    Log.e(TAG, "Camera switch error: $error")
                }
            })
        }
    }

    private fun startTimer() {
        stopTimer()
        callTimer = java.util.Timer()
        callTimer?.scheduleAtFixedRate(object : java.util.TimerTask() {
            override fun run() {
                _callDurationSeconds.value = _callDurationSeconds.value + 1
            }
        }, 1000, 1000)
    }

    private fun stopTimer() {
        callTimer?.cancel()
        callTimer = null
    }
}
