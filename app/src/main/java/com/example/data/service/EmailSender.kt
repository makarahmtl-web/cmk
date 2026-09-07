package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {
    private const val TAG = "EmailSender"

    // Custom result class to convey if sending was successful or if we need a sandbox/offline bypass
    data class SendResult(val isSuccess: Boolean, val isSandboxBypass: Boolean = false, val errorMsg: String? = null)

    suspend fun sendOtpEmail(toEmail: String, otp: String, isReset: Boolean): SendResult = withContext(Dispatchers.IO) {
        val user = BuildConfig.GMAIL_ADDRESS.trim()
        val pass = BuildConfig.GMAIL_APP_PASSWORD.trim()

        Log.d(TAG, "Sending OTP using address: $user")
        if (user.isEmpty() || pass.isEmpty()) {
            Log.e(TAG, "Gmail Address or App Password is not set in BuildConfig!")
            return@withContext SendResult(false, isSandboxBypass = true, errorMsg = "Credentials empty. Using Sandbox Bypass.")
        }

        val props = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.socketFactory.port", "465")
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.auth", "true")
            put("mail.smtp.port", "465")
            // Set reasonable timeout so it fails quickly instead of hanging
            put("mail.smtp.connectiontimeout", "4000")
            put("mail.smtp.timeout", "4000")
        }

        try {
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(user, pass)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(user, "CMK Construction"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                subject = if (isReset) {
                    "🔑 OTP សម្រាប់ប្តូរលេខសម្ងាត់ថ្មី (Password Reset Verification)"
                } else {
                    "🛡️ OTP សម្រាប់ចុះឈ្មោះគណនីថ្មី (Account Registration)"
                }
                
                val content = """
                    <div style="font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 25px; border: 1px solid #e2e8f0; border-radius: 16px; background-color: #ffffff; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);">
                        <div style="text-align: center; margin-bottom: 20px;">
                            <h2 style="color: #1e3a8a; margin: 0; font-size: 24px; font-weight: 800;">CMK Construction</h2>
                            <p style="color: #d97706; margin: 5px 0 0 0; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;">Quality Materials Import & Distribute</p>
                        </div>
                        <hr style="border: 0; border-top: 1px solid #e2e8f0; margin-bottom: 25px;">
                        <p style="font-size: 16px; color: #334155; line-height: 1.6; margin-bottom: 20px;">
                            សួស្តីបង! នេះជាលេខកូដបញ្ជាក់សុវត្ថិភាព OTP របស់បង៖
                        </p>
                        <div style="text-align: center; background-color: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 12px; padding: 15px; margin: 25px 0;">
                            <span style="font-size: 32px; font-weight: 800; color: #1e3a8a; letter-spacing: 6px;">$otp</span>
                        </div>
                        <p style="font-size: 13px; color: #64748b; line-height: 1.6; margin-top: 25px; text-align: center;">
                            * លេខកូដ OTP នេះមានសុពលភាពត្រឹមតែ ១០ នាទីប៉ុណ្ណោះ។ សូមកុំចែករំលែកលេខកូដនេះទៅកាន់អ្នកដទៃ។
                        </p>
                        <hr style="border: 0; border-top: 1px solid #e2e8f0; margin-top: 25px; margin-bottom: 15px;">
                        <p style="font-size: 11px; color: #94a3b8; text-align: center; margin: 0;">
                            CMK Construction Co., Ltd. &copy; 2026. All rights reserved.
                        </p>
                    </div>
                """.trimIndent()

                setContent(content, "text/html; charset=utf-8")
            }

            Transport.send(message)
            Log.d(TAG, "OTP Email sent successfully to $toEmail!")
            SendResult(isSuccess = true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email to $toEmail due to connection/auth limits, using sandbox fallback.", e)
            // Return sandbox bypass so the developer/user is not stuck when internet outbound port 465 is blocked by server/sandbox
            SendResult(isSuccess = false, isSandboxBypass = true, errorMsg = e.message)
        }
    }
}
