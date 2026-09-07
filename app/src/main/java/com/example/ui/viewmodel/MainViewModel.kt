package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.*
import com.example.service.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    FEED, CATALOGUE, CALL_LOG, CHAT, PROFILE, SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    // Repositories & Services
    val authRepository = AuthRepository()
    val firestoreRepository = FirestoreRepository()
    val supabaseRepository = SupabaseRepository()
    val webRtcService = WebRtcService(application)

    // Security Passcode (App Password) Management
    private val prefs = application.getSharedPreferences("cmk_security_prefs", android.content.Context.MODE_PRIVATE)

    private val _hasPasscode = MutableStateFlow(prefs.getString("app_passcode", null) != null)
    val hasPasscode: StateFlow<Boolean> = _hasPasscode.asStateFlow()

    private val _isAppLocked = MutableStateFlow(prefs.getString("app_passcode", null) != null)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // --- RECENTLY ADDED SETTINGS STATES ---
    private val _appTheme = MutableStateFlow(prefs.getString("app_theme", "AUTO") ?: "AUTO")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString("app_lang", "KHMER") ?: "KHMER")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _environmentalMode = MutableStateFlow(prefs.getString("env_mode", "CLEAR") ?: "CLEAR")
    val environmentalMode: StateFlow<String> = _environmentalMode.asStateFlow()

    private val _biometricsEnabled = MutableStateFlow(prefs.getBoolean("biometrics_enabled", false))
    val biometricsEnabled: StateFlow<Boolean> = _biometricsEnabled.asStateFlow()

    private val _blockedUsers = MutableStateFlow<Set<String>>(
        prefs.getStringSet("blocked_users", setOf("Siam Cement Co., Ltd.", "Thai Steel Trading Ltd.")) ?: setOf("Siam Cement Co., Ltd.", "Thai Steel Trading Ltd.")
    )
    val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    fun updateTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
        _appTheme.value = theme
    }

    fun updateLanguage(lang: String) {
        prefs.edit().putString("app_lang", lang).apply()
        _appLanguage.value = lang
    }

    fun updateEnvironmentalMode(mode: String) {
        prefs.edit().putString("env_mode", mode).apply()
        _environmentalMode.value = mode
    }

    fun toggleBiometrics(enabled: Boolean) {
        prefs.edit().putBoolean("biometrics_enabled", enabled).apply()
        _biometricsEnabled.value = enabled
    }

    fun blockUser(userName: String) {
        val updated = _blockedUsers.value.toMutableSet()
        updated.add(userName)
        prefs.edit().putStringSet("blocked_users", updated).apply()
        _blockedUsers.value = updated

        val user = currentUserSession.value
        if (user != null) {
            val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
                firestoreRepository.blockUserInFirestore(user.uid, userName)
            }
        }
    }

    fun unblockUser(userName: String) {
        val updated = _blockedUsers.value.toMutableSet()
        updated.remove(userName)
        prefs.edit().putStringSet("blocked_users", updated).apply()
        _blockedUsers.value = updated

        val user = currentUserSession.value
        if (user != null) {
            val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
                firestoreRepository.unblockUserInFirestore(user.uid, userName)
            }
        }
    }

    // --- 100% OFFICIAL TRANSLATION DICTIONARY (PREVENTING CROSS-CONTAMINATION) ---
    private val translations = mapOf(
        "KHMER" to mapOf(
            "app_name" to "CMK Corporate Hub",
            "feed_tab" to "មតិព័ត៌មាន",
            "store_tab" to "ហាងទំនិញ",
            "call_tab" to "មិត្តភក្តិ",
            "chat_tab" to "សារជជែក",
            "profile_tab" to "ប្រវត្តិរូប",
            "settings_tab" to "ការកំណត់",
            "settings_title" to "ការកំណត់ និងឯកជនភាព",
            "settings_subtitle" to "គ្រប់គ្រងស្បែកកម្មវិធី ភាសា និងប្រព័ន្ធសុវត្ថិភាព",
            "edit_profile" to "កែសម្រួលប្រវត្តិរូបជាសាធារណៈ",
            "personal_info" to "ព័ត៌មានផ្ទាល់ខ្លួន និងគណនី",
            "app_lock" to "ការចាក់សោកម្មវិធី និងលេខកូដសម្ងាត់ PIN",
            "change_password" to "ផ្លាស់ប្តូរលេខកូដសម្ងាត់",
            "active_sessions" to "រក្សាទុកព័ត៌មានចូល និងវគ្គសកម្ម",
            "notifications" to "ការកំណត់ការជូនដំណឹង",
            "saved_media" to "តំណភ្ជាប់រហ័សឯកសារ និងសេចក្តីព្រាង",
            "logout" to "ចាកចេញពីគណនី",
            "theme" to "ស្បែកកម្មវិធី (Theme)",
            "theme_light" to "ថ្ងៃ (Light)",
            "theme_dark" to "យប់ (Dark)",
            "theme_auto" to "ស្វ័យប្រវត្តិ (Auto)",
            "language" to "ភាសា (Language)",
            "env_mode" to "បរិយាកាស (Environmental Mode)",
            "env_clear" to "ស្រឡះ (Clear)",
            "env_rainy" to "មេឃភ្លៀង (Rainy)",
            "env_windy" to "ខ្យល់ (Windy)",
            "security" to "មុខងារសុវត្ថិភាព (Security)",
            "biometrics" to "ស្កេនក្រយៅដៃ (Biometrics)",
            "blocking" to "ឯកជនភាពនៃការទប់ស្កាត់",
            "blocking_manage" to "គ្រប់គ្រងបញ្ជីប្លុកគណនី (Block/Unblock)",
            "blocked_list" to "ដៃគូសាជីវកម្មដែលត្រូវបានប្លុក",
            "block_btn" to "ប្លុកដៃគូនេះ",
            "unblock_btn" to "លុបប្លុក",
            "wholesale_catalog" to "ទំនិញនាំចូល និងគ្រឿងសំណង់បោះដុំ",
            "search_placeholder" to "ស្វែងរកគ្រឿងសំណង់បោះដុំ...",
            "deliver_near" to "ដឹកជញ្ជូនទៅកាន់ការដ្ឋានសំណង់របស់អ្នក៖",
            "availability" to "ស្ថានភាពទំនិញ",
            "post_material" to "បង្ហោះលក់ទំនិញគ្រឿងសំណង់",
            "product_desc" to "ព័ត៌មានលម្អិត និងការពិពណ៌នាអំពីទំនិញ",
            "material_name" to "ឈ្មោះទំនិញគ្រឿងសំណង់",
            "category" to "ប្រភេទមុខទំនិញ",
            "wholesale_price" to "តម្លៃលក់បោះដុំ",
            "specs" to "លក្ខណៈបច្ចេកទេស និងទំហំ",
            "attached_images" to "រូបភាពភ្ជាប់ជាមួយទំនិញ",
            "selected_location" to "ទីតាំងដែលបានជ្រើសរើស",
            "add_post" to "បង្ហោះផ្សាយទំនិញ",
            "no_match" to "មិនមានទំនិញត្រូវនឹងការស្វែងរក ឬទីតាំងទេ",
            "no_match_sub" to "សូមព្យាយាមលុបតម្រង ឬផ្លាស់ប្តូរតំបន់។"
        ),
        "ENGLISH" to mapOf(
            "app_name" to "CMK Corporate Hub",
            "feed_tab" to "Feed",
            "store_tab" to "Store",
            "call_tab" to "Friends",
            "chat_tab" to "Chat Inbox",
            "profile_tab" to "My Profile",
            "settings_tab" to "Settings",
            "settings_title" to "Settings & Privacy",
            "settings_subtitle" to "Manage application theme, language, and security metrics",
            "edit_profile" to "Edit Public Profile Profile",
            "personal_info" to "Personal & Account Details",
            "app_lock" to "App Lock & PIN Code Access",
            "change_password" to "Modify Secure Password",
            "active_sessions" to "Saved Logins & Active Work Sessions",
            "notifications" to "Notification Alerts & Sounds",
            "saved_media" to "Saved Materials & Offline Drafts",
            "logout" to "Terminate Session (Log Out)",
            "theme" to "System Visual Theme",
            "theme_light" to "Light Mode Active",
            "theme_dark" to "Dark Mode Active",
            "theme_auto" to "Automatic System Sync",
            "language" to "Official Application Language",
            "env_mode" to "Environmental Site Mode",
            "env_clear" to "Clear Sunny Skies",
            "env_rainy" to "Torrential Downpour Rainy",
            "env_windy" to "Gale-Force Windy Storms",
            "security" to "Security and Access Controls",
            "biometrics" to "Biometric Fingerprint Unlock",
            "blocking" to "User Blocking & Blacklists",
            "blocking_manage" to "Manage Blocked Partners (Block/Unblock)",
            "blocked_list" to "Blocked Corporate Partners",
            "block_btn" to "Block Partner Listing",
            "unblock_btn" to "Unblock Now",
            "wholesale_catalog" to "Imported & Wholesale Construction Materials",
            "search_placeholder" to "Search wholesale catalogue...",
            "deliver_near" to "Deliver near your construction yard:",
            "availability" to "Item Availability Status",
            "post_material" to "Publish Wholesale Material Listing",
            "product_desc" to "Material Specifications & Detailed Descriptions",
            "material_name" to "Material Listing Name",
            "category" to "Material Category Tag",
            "wholesale_price" to "Wholesale Commercial Price",
            "specs" to "Specifications, Size and Certification",
            "attached_images" to "Attached Listing Photography",
            "selected_location" to "Registered Warehouse Location",
            "add_post" to "Publish Post",
            "no_match" to "No materials match search criteria or region",
            "no_match_sub" to "Try clearing filters or switching provinces."
        ),
        "VIETNAMESE" to mapOf(
            "app_name" to "Cổng Thông Tin CMK",
            "feed_tab" to "Bản Tin Dự Án",
            "store_tab" to "Cửa Hàng Vật Tư",
            "call_tab" to "Nhật Ký Gọi",
            "chat_tab" to "Hộp Thư Chat",
            "profile_tab" to "Hồ Sơ Đối Tác",
            "settings_tab" to "Thiết Lập Hệ Thống",
            "settings_title" to "Cài Đặt & Quyền Riêng Tư",
            "settings_subtitle" to "Quản lý giao diện, ngôn ngữ chính thức và bảo mật",
            "edit_profile" to "Chỉnh sửa hồ sơ đối tác công khai",
            "personal_info" to "Thông tin tài khoản và giấy phép doanh nghiệp",
            "app_lock" to "Khóa ứng dụng bằng mã PIN an toàn",
            "change_password" to "Thay đổi mật khẩu đăng nhập",
            "active_sessions" to "Phiên hoạt động và thiết bị đã lưu",
            "notifications" to "Cấu hình thông báo tức thì",
            "saved_media" to "Tài liệu lưu trữ và bản nháp ngoại tuyến",
            "logout" to "Đăng xuất tài khoản an toàn",
            "theme" to "Giao diện ứng dụng",
            "theme_light" to "Chế độ Ban Ngày",
            "theme_dark" to "Chế độ Ban Đêm",
            "theme_auto" to "Tự động theo hệ thống",
            "language" to "Ngôn ngữ chính thức duy nhất",
            "env_mode" to "Thời tiết công trường xây dựng",
            "env_clear" to "Thời tiết Nắng Ráo",
            "env_rainy" to "Thời tiết Mưa Lớn",
            "env_windy" to "Thời tiết Gió Lộng",
            "security" to "Bảo mật & Xác thực sinh trắc",
            "biometrics" to "Quét vân tay sinh trắc học",
            "blocking" to "Chặn & Quyền Riêng Tư",
            "blocking_manage" to "Quản lý danh sách chặn (Chặn/Mở chặn)",
            "blocked_list" to "Danh sách đối tác xây dựng bị chặn",
            "block_btn" to "Chặn đối tác này",
            "unblock_btn" to "Bỏ chặn đối tác",
            "wholesale_catalog" to "Danh mục vật liệu xây dựng nhập khẩu sỉ",
            "search_placeholder" to "Tìm kiếm vật liệu xây dựng...",
            "deliver_near" to "Vận chuyển đến gần công trường của bạn:",
            "availability" to "Trạng thái sẵn có của vật tư",
            "post_material" to "Đăng bán vật liệu xây dựng sỉ",
            "product_desc" to "Thông tin chi tiết và mô tả sản phẩm",
            "material_name" to "Tên vật tư xây dựng sỉ",
            "category" to "Danh mục phân loại vật liệu",
            "wholesale_price" to "Giá bán sỉ thương mại",
            "specs" to "Thông số kỹ thuật và kích thước kiểm định",
            "attached_images" to "Hình ảnh thực tế đính kèm",
            "selected_location" to "Địa điểm lưu kho đã đăng ký",
            "add_post" to "Đăng bài ngay",
            "no_match" to "Không tìm thấy vật tư phù hợp tại khu vực này",
            "no_match_sub" to "Vui lòng xóa bộ lọc hoặc đổi tỉnh thành."
        ),
        "INDONESIAN" to mapOf(
            "app_name" to "CMK Corporate Hub",
            "feed_tab" to "Kabar Berita",
            "store_tab" to "Toko Material",
            "call_tab" to "Riwayat Panggilan",
            "chat_tab" to "Kotak Masuk Chat",
            "profile_tab" to "Profil Kemitraan",
            "settings_tab" to "Pengaturan Aplikasi",
            "settings_title" to "Pengaturan & Privasi",
            "settings_subtitle" to "Kelola tema visual, bahasa resmi, dan keamanan PIN",
            "edit_profile" to "Ubah Profil Publik Korporat",
            "personal_info" to "Informasi Akun & Izin Korporasi",
            "app_lock" to "Kunci Aplikasi dengan Kode PIN",
            "change_password" to "Ubah Kata Sandi Keamanan",
            "active_sessions" to "Perangkat Tersimpan & Sesi Aktif",
            "notifications" to "Pengaturan Notifikasi Alert",
            "saved_media" to "Berkas Tersimpan & Draf Offline",
            "logout" to "Keluar dari Akun Korporat",
            "theme" to "Tema Visual Aplikasi",
            "theme_light" to "Mode Terang (Siang)",
            "theme_dark" to "Mode Gelap (Malam)",
            "theme_auto" to "Sinkronisasi Otomatis Sistem",
            "language" to "Bahasa Resmi Aplikasi",
            "env_mode" to "Mode Lingkungan Proyek",
            "env_clear" to "Langit Cerah Berawan",
            "env_rainy" to "Cuaca Hujan Lebat",
            "env_windy" to "Cuaca Angin Kencang",
            "security" to "Keamanan & Penguncian Sistem",
            "biometrics" to "Pemindai Sidik Jari Biometrik",
            "blocking" to "Blokir & Privasi Pengguna",
            "blocking_manage" to "Kelola Pengguna Diblokir (Blokir/Buka)",
            "blocked_list" to "Daftar Mitra Konstruksi Diblokir",
            "block_btn" to "Blokir Mitra Ini",
            "unblock_btn" to "Buka Blokir",
            "wholesale_catalog" to "Daftar Grosir Material Konstruksi Impor",
            "search_placeholder" to "Cari material grosir...",
            "deliver_near" to "Kirim ke dekat lokasi proyek Anda:",
            "availability" to "Status Ketersediaan Material",
            "post_material" to "Pasang Iklan Grosir Material",
            "product_desc" to "Spesifikasi Produk & Deskripsi Detail",
            "material_name" to "Nama Item Material Grosir",
            "category" to "Kategori Klasifikasi Material",
            "wholesale_price" to "Harga Grosir Komersial",
            "specs" to "Spesifikasi Teknis & Dimensi Standard",
            "attached_images" to "Foto Lampiran Produk",
            "selected_location" to "Lokasi Gudang Terdaftar",
            "add_post" to "Pasang Iklan",
            "no_match" to "Tidak ada material grosir di wilayah ini",
            "no_match_sub" to "Silakan hapus filter atau ganti provinsi."
        ),
        "LAO" to mapOf(
            "app_name" to "CMK Corporate Hub",
            "feed_tab" to "ຂ່າວສານໂຄງການ",
            "store_tab" to "ຮ້ານຄ້າວັດສະດຸ",
            "call_tab" to "ບັນທຶກການໂທ",
            "chat_tab" to "ຫ້ອງສົນທະນາ",
            "profile_tab" to "ໂປຣໄຟລ໌ຄູ່ຄ້າ",
            "settings_tab" to "ການຕັ້ງຄ່າ",
            "settings_title" to "ການຕັ້ງຄ່າ ແລະ ຄວາມເປັນສ່ວນຕົວ",
            "settings_subtitle" to "ຈັດການຮູບແບບແອັບ, ພາສາທາງການ ແລະ ຄວາມປອດໄພ PIN",
            "edit_profile" to "ແກ້ໄຂໂປຣໄຟລ໌ສາທາລະນະ",
            "personal_info" to "ຂໍ້ມູນສ່ວນຕົວ ແລະ ສິດທິອົງກອນ",
            "app_lock" to "ລັອກແອັບດ້ວຍລະຫັດ PIN ປອດໄພ",
            "change_password" to "ປ່ຽນລະຫັດຜ່ານຄວາມປອດໄພ",
            "active_sessions" to "ອຸປະກອນທີ່ບັນທຶກ ແລະ ເຊດຊັນທີ່ໃຊ້ງານ",
            "notifications" to "ການຕັ້ງຄ່າການແຈ້ງເຕືອນ",
            "saved_media" to "ເອກະສານທີ່ບັນທຶກ ແລະ ຮ່າງອອຟລາຍ",
            "logout" to "ອອກຈາກລະບົບຢ່າງປອດໄພ",
            "theme" to "ຮູບແບບສີສັນຂອງແອັບ",
            "theme_light" to "ໂໝດກາງເວັນ (ແຈ້ງ)",
            "theme_dark" to "ໂໝດກາງຄືນ (ມືດ)",
            "theme_auto" to "ປັບອັດຕະໂນມັດຕາມລະບົບ",
            "language" to "ພາສາທາງການຂອງແອັບ",
            "env_mode" to "ສະພາບແວດລ້ອມເຂດກໍ່ສ້າງ",
            "env_clear" to "ທ້ອງຟ້າແຈ່ມໃສດີ",
            "env_rainy" to "ສະພາບຝົນຕົກໜັກ",
            "env_windy" to "ສະພາບລົມພັດແຮງ",
            "security" to "ລະບົບຄວາມປອດໄພ ແລະ ການລັອກ",
            "biometrics" to "ເຄື່ອງສະແກນລາຍນິ້ວມື",
            "blocking" to "ການບລັອກຄູ່ຄ້າ ແລະ ຄວາມເປັນສ່ວນຕົວ",
            "blocking_manage" to "ຈັດການລາຍຊື່ບລັອກ (ບລັອກ/ປົດບລັອກ)",
            "blocked_list" to "ລາຍຊື່ຄູ່ຄ້າກໍ່ສ້າງທີ່ຖືກບລັອກ",
            "block_btn" to "ບລັອກຄູ່ຄ້ານີ້",
            "unblock_btn" to "ປົດບລັອກ",
            "wholesale_catalog" to "ລາຍການວັດສະດຸກໍ່ສ້າງຂາຍສົ່ງນຳເຂົ້າ",
            "search_placeholder" to "ຄົ້ນຫາວັດສະດຸຂາຍສົ່ງ...",
            "deliver_near" to "ຈັດສົ່ງໄປໃກ້ກັບສະຖານທີ່ກໍ່ສ້າງຂອງທ່ານ:",
            "availability" to "ສະຖານະຄວາມພ້ອມຂອງວັດສະດຸ",
            "post_material" to "ລົງທະບຽນຂາຍວັດສະດຸຂາຍສົ່ງ",
            "product_desc" to "ລາຍລະອຽດວັດສະດຸ ແລະ ການອະທິບາຍ",
            "material_name" to "ຊື່ລາຍການວັດສະດຸຂາຍສົ່ງ",
            "category" to "ໝວດໝູ່ການຈັດແບ່ງວັດສະດຸ",
            "wholesale_price" to "ລາຄາຂາຍສົ່ງການຄ້າ",
            "specs" to "ຂໍ້ມູນດ້ານເຕັກນິກ ແລະ ຂະໜາດມາດຕະຖານ",
            "attached_images" to "ຮູບພາບປະກອບທີ່ຄັດຕິດ",
            "selected_location" to "ສະຖານທີ່ສາງທີ່ລົງທະບຽນ",
            "add_post" to "ລົງປະກາດ",
            "no_match" to "ບໍ່ພົບວັດສະດຸກໍ່ສ້າງທີ່ກົງກັນໃນເຂດນີ້",
            "no_match_sub" to "ກະລຸນາລຶບຕົວກອງ ຫຼື ປ່ຽນແຂວງອື່ນ."
        ),
        "BURMESE" to mapOf(
            "app_name" to "CMK Corporate Hub",
            "feed_tab" to "သတင်းအချက်အလက်",
            "store_tab" to "ပစ္စည်းအရောင်းဆိုင်",
            "call_tab" to "ဖုန်းခေါ်ဆိုမှုမှတ်တမ်း",
            "chat_tab" to "စကားပြောခန်း",
            "profile_tab" to "လုပ်ငန်းရှင်ပရိုဖိုင်း",
            "settings_tab" to "ဆက်တင်များ",
            "settings_title" to "ဆက်တင်နှင့် ကိုယ်ရေးလုံခြုံမှု",
            "settings_subtitle" to "အက်ပ်နောက်ခံ၊ တရားဝင်ဘာသာစကားနှင့် PIN လုံခြုံရေး စီမံခြင်း",
            "edit_profile" to "အများမြင်ပရိုဖိုင်း ပြင်ဆင်ရန်",
            "personal_info" to "ကိုယ်ရေးအချက်အလက်နှင့် ခွင့်ပြုချက်များ",
            "app_lock" to "အက်ပ်လော့ခ်နှင့် လုံခြုံသော PIN ကုဒ်",
            "change_password" to "လျှို့ဝှက်နံပါတ် ပြောင်းလဲရန်",
            "active_sessions" to "မှတ်သားထားသော စက်ပစ္စည်းများနှင့် အသုံးပြုမှုများ",
            "notifications" to "သတိပေးချက် ဆက်တင်များ",
            "saved_media" to "သိမ်းဆည်းထားသော ပစ္စည်းများနှင့် မူကြမ်းများ",
            "logout" to "လုပ်ငန်းသုံးအကောင့်မှ ထွက်ရန်",
            "theme" to "အက်ပ်၏ နောက်ခံအရောင်",
            "theme_light" to "နေ့ဘက်မုဒ် (အလင်း)",
            "theme_dark" to "ညဘက်မုဒ် (အမှောင်)",
            "theme_auto" to "စနစ်နှင့်အလိုအလျောက်ချိတ်ဆက်ရန်",
            "language" to "အက်ပ်၏ တရားဝင်ဘာသာစကား",
            "env_mode" to "ဆောက်လုပ်ရေးလုပ်ငန်းခွင် ရာသီဥတုမုဒ်",
            "env_clear" to "သာယာကြည်လင်သော ရာသီဥတု",
            "env_rainy" to "သည်းထန်စွာ မိုးရွာသွန်းခြင်း",
            "env_windy" to "ပြင်းထန်သော လေပြင်းတိုက်ခတ်ခြင်း",
            "security" to "လုံခြုံရေးစနစ်နှင့် လော့ခ်ချခြင်း",
            "biometrics" to "လက်ဗွေရာစနစ်ဖြင့် စကန်ဖတ်ခြင်း",
            "blocking" to "မိတ်ဖက်များ ပိတ်ဆို့ခြင်းနှင့် ကိုယ်ရေးလုံခြုံမှု",
            "blocking_manage" to "ပိတ်ဆို့ထားသူများ စီမံရန် (ပိတ်ဆို့/ပြန်ဖွင့်)",
            "blocked_list" to "ပိတ်ဆို့ထားသော ဆောက်လုပ်ရေးမိတ်ဖက်များ",
            "block_btn" to "ဤမိတ်ဖက်အား ပိတ်ဆို့ရန်",
            "unblock_btn" to "ပိတ်ဆို့မှု ပြန်ဖွင့်ရန်",
            "wholesale_catalog" to "တင်သွင်းလာသော လက်ကားဆောက်လုပ်ရေးပစ္စည်းများ",
            "search_placeholder" to "လက်ကားပစ္စည်းများ ရှာဖွေရန်...",
            "deliver_near" to "သင်၏ဆောက်လုပ်ရေးလုပ်ငန်းခွင်အနီးသို့ ပို့ဆောင်ရန်-",
            "availability" to "ပစ္စည်းရရှိနိုင်မှု အခြေအနေ",
            "post_material" to "လက်ကားပစ္စည်းအရောင်း တင်ရန်",
            "product_desc" to "ပစ္စည်းအသေးစိတ်နှင့် ဖော်ပြချက်များ",
            "material_name" to "လက်ကားပစ္စည်းအမည်",
            "category" to "ပစ္စည်းအမျိုးအစား သတ်မှတ်ချက်",
            "wholesale_price" to "လက်ကားဈေးနှုန်း သတ်မှတ်ချက်",
            "specs" to "နည်းပညာပိုင်းဆိုင်ရာ အချက်အလက်နှင့် အရွယ်အစား",
            "attached_images" to "ပူးတွဲဓာတ်ပုံများ",
            "selected_location" to "မှတ်ပုံတင်ထားသော သိုလှောင်ရုံတည်နေရာ",
            "add_post" to "ကြော်ငြာတင်ရန်",
            "no_match" to "ဤဒေသတွင် ကိုက်ညီသော ဆောက်လုပ်ရေးပစ္စည်း မတွေ့ပါ",
            "no_match_sub" to "စစ်ထုတ်မှုကို ဖျက်ပါ သို့မဟုတ် အခြားပြည်နယ် ပြောင်းပါ။"
        ),
        "FILIPINO" to mapOf(
            "app_name" to "CMK Corporate Hub",
            "feed_tab" to "Mga Balita",
            "store_tab" to "Tindahan ng Materyales",
            "call_tab" to "Talaan ng Tawag",
            "chat_tab" to "Inbox ng Chat",
            "profile_tab" to "Profile ng Kasosyo",
            "settings_tab" to "Mga Setting",
            "settings_title" to "Mga Setting at Privacy",
            "settings_subtitle" to "Pamahalaan ang tema, wikang opisyal, at PIN security",
            "edit_profile" to "I-edit ang Pampublikong Profile",
            "personal_info" to "Impormasyon ng Account at Mga Permiso",
            "app_lock" to "Lock ng App at Secure PIN Code",
            "change_password" to "Baguhin ang Password",
            "active_sessions" to "Mga Naka-save na Device at Aktibong Session",
            "notifications" to "Mga Setting ng Abiso",
            "saved_media" to "Mga Naka-save na Dokumento at Drafts",
            "logout" to "Ligtas na Mag-log Out",
            "theme" to "Visual na Tema ng App",
            "theme_light" to "Maliwanag na Mode (Araw)",
            "theme_dark" to "Madilim na Mode (Gabi)",
            "theme_auto" to "Awtomatikong Kasabay ng System",
            "language" to "Opisyal na Wika ng App",
            "env_mode" to "Kondisyon sa Lokasyon ng Konstruksyon",
            "env_clear" to "Maayos at Maaraw na Panahon",
            "env_rainy" to "Malakas na Pag-ulan",
            "env_windy" to "Malakas na Hangin",
            "security" to "Seguridad at Lock ng System",
            "biometrics" to "Scanner ng Biometric Fingerprint",
            "blocking" to "Pag-block at Privacy ng User",
            "blocking_manage" to "Pamahalaan ang Mga Naka-block (I-block/I-unblock)",
            "blocked_list" to "Mga Naka-block na Kasosyo sa Konstruksyon",
            "block_btn" to "I-block ang Kasosyo na Ito",
            "unblock_btn" to "I-unblock",
            "wholesale_catalog" to "Listahan ng Pakyawan ng mga Inangkat na Materyales",
            "search_placeholder" to "Maghanap ng pakyawang materyales...",
            "deliver_near" to "Ihatid malapit sa iyong construction yard:",
            "availability" to "Katayuan ng Supply ng Materyales",
            "post_material" to "Mag-post ng Pakyawan na Materyales",
            "product_desc" to "Mga Detalye ng Produkto at Paglalarawan",
            "material_name" to "Pangalan ng Pakyawang Materyales",
            "category" to "Kategorya ng Klasipikasyon",
            "wholesale_price" to "Pakyawan Presyong Komersyal",
            "specs" to "Teknikal na Detalye at Standard na Sukat",
            "attached_images" to "Mga Nakalakip na Larawan",
            "selected_location" to "Nakarehistrong Lokasyon ng Bodega",
            "add_post" to "I-publish ang Post",
            "no_match" to "Walang tumutugmang materyales sa rehiyong ito",
            "no_match_sub" to "Subukang burahin ang mga filter o magpalit ng probinsya."
        ),
        "MALAY" to mapOf(
            "app_name" to "CMK Corporate Hub",
            "feed_tab" to "Suapan Berita",
            "store_tab" to "Gudang Material",
            "call_tab" to "Log Panggilan",
            "chat_tab" to "Peti Masuk Sembang",
            "profile_tab" to "Profil Rakan Kongsi",
            "settings_tab" to "Tetapan Sistem",
            "settings_title" to "Tetapan & Privasi",
            "settings_subtitle" to "Urus tema visual, bahasa rasmi, dan keselamatan PIN",
            "edit_profile" to "Ubah Suai Profil Awam Rakan Kongsi",
            "personal_info" to "Maklumat Peribadi & Kebenaran Syarikat",
            "app_lock" to "Kunci Aplikasi dengan Kod PIN Latar",
            "change_password" to "Tukar Kata Laluan Keselamatan",
            "active_sessions" to "Peranti Tersimpan & Sesi Aktif Semasa",
            "notifications" to "Tetapan Amaran Notifikasi",
            "saved_media" to "Dokumen Tersimpan & Draf Luar Talian",
            "logout" to "Log Keluar Sesi Korporat",
            "theme" to "Tema Visual Aplikasi Utama",
            "theme_light" to "Mod Cerah (Siang Hari)",
            "theme_dark" to "Mod Gelap (Malam Hari)",
            "theme_auto" to "Penyelarasan Automatik Sistem",
            "language" to "Bahasa Rasmi Aplikasi Tunggal",
            "env_mode" to "Mod Persekitaran Tapak Projek",
            "env_clear" to "Cuaca Cerah dan Lapang",
            "env_rainy" to "Cuaca Hujan Lebat Turun",
            "env_windy" to "Cuaca Angin Bertiup Kencang",
            "security" to "Sistem Keselamatan & Kunci Akses",
            "biometrics" to "Pengimbas Cap Jari Biometrik",
            "blocking" to "Sekatan & Privasi Pengguna",
            "blocking_manage" to "Urus Senarai Sekatan (Sekat/Buka)",
            "blocked_list" to "Senarai Rakan Kongsi Sekatan",
            "block_btn" to "Sekat Rakan Kongsi Ini",
            "unblock_btn" to "Buka Sekatan",
            "wholesale_catalog" to "Daftar Borong Bahan Binaan Import",
            "search_placeholder" to "Cari bahan binaan borong...",
            "deliver_near" to "Hantar ke kawasan berhampiran tapak projek:",
            "availability" to "Status Ketersediaan Bekalan Bahan",
            "post_material" to "Iklankan Bahan Binaan Borong",
            "product_desc" to "Spesifikasi Produk & Huraian Terperinci",
            "material_name" to "Nama Item Bahan Binaan Borong",
            "category" to "Kategori Pengelasan Bahan",
            "wholesale_price" to "Harga Borong Komersial",
            "specs" to "Spesifikasi Teknikal & Ukuran Standard",
            "attached_images" to "Foto Lampiran Produk",
            "selected_location" to "Lokasi Gudang Terdaftar Syarikat",
            "add_post" to "Iklankan Sekarang",
            "no_match" to "Tiada bahan binaan sepadan di kawasan ini",
            "no_match_sub" to "Sila padam penapis atau tukar negeri lain."
        ),
        "SINGAPOREAN" to mapOf(
            "app_name" to "CMK Corporate Hub",
            "feed_tab" to "Updates Feed",
            "store_tab" to "Wholesale Store",
            "call_tab" to "Call History",
            "chat_tab" to "Chat Box",
            "profile_tab" to "Partner Profile",
            "settings_tab" to "Settings",
            "settings_title" to "Settings & Privacy Dashboard",
            "settings_subtitle" to "Manage visual theme, language, and PIN password features",
            "edit_profile" to "Edit Public Profile",
            "personal_info" to "Personal Info & Corporate Clearance",
            "app_lock" to "App Lock & Secure PIN setup",
            "change_password" to "Change Security Password",
            "active_sessions" to "Saved Logins & Active Sessions",
            "notifications" to "Notification Alerts Settings",
            "saved_media" to "Saved Materials & Local Drafts",
            "logout" to "Terminate Corporate Session (Log Out)",
            "theme" to "App Visual Theme Preference",
            "theme_light" to "Light Mode Standard",
            "theme_dark" to "Dark Mode Standard",
            "theme_auto" to "Automatic System Sync",
            "language" to "Official Platform Language",
            "env_mode" to "Environmental Site Conditions",
            "env_clear" to "Clear Sunny Weather",
            "env_rainy" to "Heavy Rain Storm",
            "env_windy" to "Very Windy Conditions",
            "security" to "Security Locks & Credentials",
            "biometrics" to "Biometric Fingerprint Scanner",
            "blocking" to "Partner Blocking Controls",
            "blocking_manage" to "Manage Blocked Partners (Block/Unblock)",
            "blocked_list" to "Blocked Corporate List",
            "block_btn" to "Block Partner Listing",
            "unblock_btn" to "Unblock",
            "wholesale_catalog" to "Imported & Wholesale Builders Materials",
            "search_placeholder" to "Search wholesale catalogue...",
            "deliver_near" to "Deliver near your construction yard:",
            "availability" to "Item Supply Availability",
            "post_material" to "Publish Wholesale Material Listing",
            "product_desc" to "Material Specs & Description Details",
            "material_name" to "Material Listing Name",
            "category" to "Material Category Tag",
            "wholesale_price" to "Wholesale Price Offered",
            "specs" to "Specifications & Certified Size",
            "attached_images" to "Attached Listing Photos",
            "selected_location" to "Registered Warehouse Location",
            "add_post" to "Publish Post",
            "no_match" to "No materials match search criteria or region",
            "no_match_sub" to "Try clearing filters or switching provinces."
        )
    )

    fun getString(key: String): String {
        val lang = _appLanguage.value
        return translations[lang]?.get(key) ?: translations["ENGLISH"]?.get(key) ?: key
    }


    fun verifyPasscode(pin: String): Boolean {
        val savedPin = prefs.getString("app_passcode", null)
        return if (savedPin == pin) {
            _isAppLocked.value = false
            true
        } else {
            false
        }
    }

    fun setPasscode(pin: String) {
        prefs.edit().putString("app_passcode", pin).apply()
        _hasPasscode.value = true
        _isAppLocked.value = false
    }

    fun clearPasscode() {
        prefs.edit().remove("app_passcode").apply()
        _hasPasscode.value = false
        _isAppLocked.value = false
    }

    fun lockApp() {
        if (_hasPasscode.value) {
            _isAppLocked.value = true
        }
    }

    // Auth State
    val currentUserSession: StateFlow<UserSession?> = authRepository.currentUserFlow

    fun updateProfile(
        displayName: String,
        bio: String,
        headline: String,
        workplace: String,
        location: String,
        avatarUrl: String?,
        coverUrl: String?
    ) {
        val current = currentUserSession.value ?: UserSession(
            uid = "user_default_uid",
            email = "partner@cmkmaterials.com",
            displayName = displayName,
            avatarInitials = if (displayName.isNotBlank()) displayName.take(2).uppercase() else "CP",
            avatarUrl = avatarUrl,
            coverUrl = coverUrl,
            bio = bio,
            headline = headline,
            workplace = workplace,
            location = location,
            joinedDate = "September 2026",
            inquiriesCount = 0,
            activeDealsCount = 0,
            feedPostsCount = 0
        )
        val initials = displayName.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "CP" }

        val updated = current.copy(
            displayName = displayName,
            avatarInitials = initials,
            bio = bio,
            headline = headline,
            workplace = workplace,
            location = location,
            avatarUrl = if (avatarUrl != null) avatarUrl else current.avatarUrl,
            coverUrl = if (coverUrl != null) coverUrl else current.coverUrl
        )
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
            authRepository.updateUserProfile(updated)
        }
    }

    fun updateProfileAvatar(newAvatarUrl: String?) {
        val current = currentUserSession.value ?: UserSession(
            uid = "user_default_uid",
            email = "partner@cmkmaterials.com",
            displayName = "CMK Partner",
            avatarInitials = "CP"
        )
        val updated = current.copy(avatarUrl = newAvatarUrl)
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
            authRepository.updateUserProfile(updated)
        }
    }

    fun updateProfileCover(newCoverUrl: String?) {
        val current = currentUserSession.value ?: UserSession(
            uid = "user_default_uid",
            email = "partner@cmkmaterials.com",
            displayName = "CMK Partner",
            avatarInitials = "CP"
        )
        val updated = current.copy(coverUrl = newCoverUrl)
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
            authRepository.updateUserProfile(updated)
        }
    }

    init {
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
            currentUserSession.collect { user ->
                if (user != null) {
                    listenToBlockedUsersFromFirestore(user.uid)
                }
            }
        }
    }

    private fun listenToBlockedUsersFromFirestore(blockerId: String) {
        firestoreRepository.listenToBlockedUsers(blockerId) { names ->
            if (names.isNotEmpty() || _blockedUsers.value.isNotEmpty()) {
                val defaultBlocked = setOf("Siam Cement Co., Ltd.", "Thai Steel Trading Ltd.")
                val finalSet = defaultBlocked + names
                prefs.edit().putStringSet("blocked_users", finalSet).apply()
                _blockedUsers.value = finalSet
            }
        }
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.FEED)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Feed / Posting State
    val feedPosts: StateFlow<List<FeedPost>> = combine(
        firestoreRepository.postsState,
        _blockedUsers
    ) { posts, blocked ->
        posts.filter { it.userDisplayName !in blocked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _activeCommentsPostId = MutableStateFlow<String?>(null)
    val activeCommentsPostId: StateFlow<String?> = _activeCommentsPostId.asStateFlow()

    val activeComments: StateFlow<List<PostComment>> = _activeCommentsPostId
        .flatMapLatest { postId ->
            if (postId == null) flowOf(emptyList())
            else firestoreRepository.getCommentsForPost(postId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Catalogue filtering & Search
    private val _selectedCategory = MutableStateFlow<MaterialCategory?>(null)
    val selectedCategory: StateFlow<MaterialCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedProvince = MutableStateFlow("រាជធានីភ្នំពេញ")
    val selectedProvince: StateFlow<String> = _selectedProvince.asStateFlow()

    private val _selectedDistrict = MutableStateFlow("ខណ្ឌដូនពេញ")
    val selectedDistrict: StateFlow<String> = _selectedDistrict.asStateFlow()

    val filteredCatalogue: StateFlow<List<MaterialItem>> = combine(
        firestoreRepository.materialCatalogueState,
        _selectedCategory,
        _searchQuery,
        _selectedProvince,
        combine(_selectedDistrict, _blockedUsers) { d, b -> d to b }
    ) { catalog, category, query, province, distAndBlocked ->
        val (district, blocked) = distAndBlocked
        catalog.filter { item ->
            val matchesCategory = category == null || item.category == category
            val matchesQuery = query.isBlank() || item.name.contains(query, ignoreCase = true) || item.specDetails.contains(query, ignoreCase = true)
            
            val itemLoc = item.location.lowercase()
            val cleanProv = province.lowercase()
            val cleanDist = district.lowercase()
            
            val matchesLocation = if (cleanProv == "all" || cleanProv == "ទាំងអស់" || cleanProv == "គ្រប់ខេត្ត/ក្រុង") {
                true
            } else {
                itemLoc.contains(cleanProv) || itemLoc.contains(cleanDist) ||
                (cleanProv.contains("ភ្នំពេញ") && itemLoc.contains("phnom penh")) ||
                (cleanProv.contains("phnom penh") && itemLoc.contains("ភ្នំពេញ"))
            }
            
            val isBlocked = blocked.any { blockedName ->
                item.name.contains(blockedName, ignoreCase = true) ||
                item.specDetails.contains(blockedName, ignoreCase = true)
            }
            
            matchesCategory && matchesQuery && matchesLocation && !isBlocked
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: MaterialCategory?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateLocation(province: String, district: String) {
        _selectedProvince.value = province
        _selectedDistrict.value = district
    }

    fun createMaterialPost(
        name: String,
        category: MaterialCategory,
        imageUrls: List<String>,
        specDetails: String,
        bulkPrice: String,
        availability: String,
        location: String
    ) {
        val newItem = MaterialItem(
            name = name,
            category = category,
            imageUrl = imageUrls.firstOrNull() ?: "https://images.unsplash.com/photo-1590069261209-f8e9b8642343?auto=format&fit=crop&q=80&w=400",
            specDetails = specDetails,
            bulkPrice = bulkPrice,
            availability = availability,
            location = location
        )
        firestoreRepository.addNewMaterialItem(newItem)
    }

    // Chat Message Thread
    val chatMessages: StateFlow<List<ChatMessage>> = firestoreRepository.chatMessagesState

    // Call Records
    val callRecords: StateFlow<List<CallRecord>> = firestoreRepository.callRecordsState

    // Authentication Actions
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    fun signIn(email: String, password: String) {
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
            _isAuthenticating.value = true
            _authError.value = null
            authRepository.signIn(email, password)
                .onFailure { _authError.value = it.message ?: "Sign in failed" }
            _isAuthenticating.value = false
        }
    }

    fun resetPassword(email: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
            _isAuthenticating.value = true
            _authError.value = null
            authRepository.resetPassword(email, newPassword)
                .onSuccess {
                    onResult(true, null)
                }
                .onFailure {
                    _authError.value = it.message ?: "Reset password failed"
                    onResult(false, it.message)
                }
            _isAuthenticating.value = false
        }
    }

    fun signUp(email: String, password: String, name: String) {
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
            _isAuthenticating.value = true
            _authError.value = null
            authRepository.signUp(email, password, name)
                .onFailure { _authError.value = it.message ?: "Registration failed" }
            _isAuthenticating.value = false
        }
    }

    fun signInAsDemoUser() {
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
        viewModelScope.launch(exceptionHandler) {
            _isAuthenticating.value = true
            _authError.value = null
            val result = authRepository.signInAsDemoUser()
            _isAuthenticating.value = false
            if (result.isFailure) {
                _authError.value = result.exceptionOrNull()?.message ?: "Demo Sign-in Failed"
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }; viewModelScope.launch(exceptionHandler) {
            _isAuthenticating.value = true
            _authError.value = null
            authRepository.signInWithGoogleCredential(idToken)
                .onFailure { _authError.value = it.message ?: "Google Sign-In failed" }
            _isAuthenticating.value = false
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentScreen.value = AppScreen.FEED // Reset to home feed
    }

    fun clearAuthError() {
        _authError.value = null
    }

    // Navigation Actions
    private val _isFeedScrollMenuVisible = MutableStateFlow(true)
    val isFeedScrollMenuVisible: StateFlow<Boolean> = _isFeedScrollMenuVisible.asStateFlow()

    fun setFeedScrollMenuVisible(visible: Boolean) {
        _isFeedScrollMenuVisible.value = visible
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        _isFeedScrollMenuVisible.value = true
    }

    // Feed Posting Actions
    // Feed Posting Actions with Image Optimization & Cloud Distribution
    fun createPost(context: android.content.Context? = null, content: String, imageUrls: List<String>, imageFilter: String? = null) {
        val user = currentUserSession.value ?: return
        com.example.data.service.AppAnalytics.logEvent("create_post", mapOf("has_images" to (imageUrls.isNotEmpty()).toString()))
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            com.example.data.service.AppAnalytics.recordException(throwable, "createPost error")
        }
        viewModelScope.launch(exceptionHandler) {
            val processedImageUrls = if (context != null && imageUrls.isNotEmpty()) {
                com.example.data.service.MediaUploadService.prepareImagesForPublishing(context, imageUrls)
            } else {
                imageUrls
            }
            val result = firestoreRepository.createPost(user, content, processedImageUrls, imageFilter)
            val post = result.getOrNull()
            if (post != null) {
                try {
                    supabaseRepository.createPost(post)
                } catch (e: Exception) {
                    // Safe Supabase fallback
                }
            }
        }
    }

    fun toggleLike(postId: String) {
        com.example.data.service.AppAnalytics.logEvent("like_post", mapOf("post_id" to postId))
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            com.example.data.service.AppAnalytics.recordException(throwable, "toggleLike error")
        }
        viewModelScope.launch(exceptionHandler) {
            firestoreRepository.toggleLike(postId)
        }
    }

    fun setCommentPostId(postId: String?) {
        _activeCommentsPostId.value = postId
    }

    fun addComment(postId: String, content: String) {
        val user = currentUserSession.value ?: return
        com.example.data.service.AppAnalytics.logEvent("add_comment", mapOf("post_id" to postId))
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            com.example.data.service.AppAnalytics.recordException(throwable, "addComment error")
        }
        viewModelScope.launch(exceptionHandler) {
            firestoreRepository.addComment(postId, user, content)
        }
    }

    // Chat Action with Image & Thread Support
    fun sendChatMessage(
        text: String,
        imageUrl: String? = null,
        threadId: String? = null
    ) {
        val user = currentUserSession.value ?: return
        com.example.data.service.AppAnalytics.logEvent("send_chat_message", mapOf(
            "length" to text.length.toString(),
            "has_image" to (imageUrl != null).toString()
        ))
        val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            com.example.data.service.AppAnalytics.recordException(throwable, "sendChatMessage error")
        }
        viewModelScope.launch(exceptionHandler) {
            firestoreRepository.sendChatMessage(user, text, imageUrl, threadId)
        }
    }

    // Call Action
    fun startCall(isVideo: Boolean, partnerName: String) {
        val record = CallRecord(
            otherPartyName = partnerName,
            type = if (isVideo) CallType.VIDEO else CallType.AUDIO,
            timeString = "Just now",
            isIncoming = false,
            durationString = "00:00"
        )
        firestoreRepository.addCallRecord(record)
        webRtcService.startCall(isVideo, partnerName)
    }

    fun endCall() {
        webRtcService.endCall()
    }

    // ==========================================
    // SUPABASE SEARCH & SOCIAL DISCOVERY
    // ==========================================

    private val _postSearchQuery = MutableStateFlow("")
    val postSearchQuery: StateFlow<String> = _postSearchQuery.asStateFlow()

    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    private val _searchedPosts = MutableStateFlow<List<FeedPost>>(emptyList())
    val searchedPosts: StateFlow<List<FeedPost>> = _searchedPosts.asStateFlow()

    private val _searchedUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchedUsers: StateFlow<List<UserProfile>> = _searchedUsers.asStateFlow()

    val followedUserIds: StateFlow<Set<String>> = supabaseRepository.followedUserIds

    fun searchPosts(query: String) {
        _postSearchQuery.value = query
        viewModelScope.launch {
            val results = supabaseRepository.searchPosts(query)
            _searchedPosts.value = results
        }
    }

    fun searchUsers(query: String) {
        _userSearchQuery.value = query
        viewModelScope.launch {
            val results = supabaseRepository.searchUsers(query)
            _searchedUsers.value = results
        }
    }

    fun toggleFollowUser(userId: String) {
        supabaseRepository.toggleFollowUser(userId)
    }
}
