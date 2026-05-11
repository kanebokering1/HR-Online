package com.binahr.ui.viewmodel


import com.binahr.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binahr.data.api.TokenManager
import com.binahr.data.api.model.*
import com.binahr.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ─── Home / Dashboard ────────────────────────────────────────────────────────

class HomeViewModel : ViewModel() {
    private val profileRepo = ProfileRepository()
    private val attendanceRepo = AttendanceRepository()

    private val _summary = MutableStateFlow<DashboardSummaryDto?>(null)
    val summary: StateFlow<DashboardSummaryDto?> = _summary.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isRefreshing.value = true
            profileRepo.getDashboardSummary()
                .onSuccess { summary ->
                    _summary.value = summary
                    // Cache employee_id from summary so ProfileViewModel can skip
                    // the expensive employees-list search on first open.
                    if (!summary.employeeId.isNullOrBlank() && TokenManager.employeeId.isNullOrBlank()) {
                        TokenManager.employeeId = summary.employeeId
                    }
                }
                .onFailure { _error.value = it.message }
            _isRefreshing.value = false
        }
    }

    fun clearError() { _error.value = null }
}

// ─── Attendance ───────────────────────────────────────────────────────────────

class AttendanceViewModel : ViewModel() {
    private val repo = AttendanceRepository()

    private val _checkInResult = MutableStateFlow<Result<AttendanceLogDto>?>(null)
    val checkInResult: StateFlow<Result<AttendanceLogDto>?> = _checkInResult.asStateFlow()

    private val _checkOutResult = MutableStateFlow<Result<AttendanceLogDto>?>(null)
    val checkOutResult: StateFlow<Result<AttendanceLogDto>?> = _checkOutResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun checkIn(lat: Double, lon: Double, address: String, selfieBase64: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _checkInResult.value = repo.checkIn(CheckInRequest(lat, lon, address, selfieBase64))
            _isLoading.value = false
        }
    }

    fun checkOut(lat: Double, lon: Double, address: String, notes: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _checkOutResult.value = repo.checkOut(CheckOutRequest(lat, lon, address, notes))
            _isLoading.value = false
        }
    }

    fun clearResults() {
        _checkInResult.value = null
        _checkOutResult.value = null
    }
}

// ─── Attendance History ───────────────────────────────────────────────────────

class HistoryViewModel : ViewModel() {
    private val repo = AttendanceRepository()

    private val _logs = MutableStateFlow<List<AttendanceLogDto>>(emptyList())
    val logs: StateFlow<List<AttendanceLogDto>> = _logs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentPage = 1
    private var lastPage = 1

    init { load() }

    fun load(reset: Boolean = true) {
        if (reset) { currentPage = 1; _logs.value = emptyList() }
        viewModelScope.launch {
            _isLoading.value = true
            repo.getLogs(currentPage)
                .onSuccess { paginated ->
                    lastPage = paginated.lastPage
                    _logs.value = if (reset) paginated.data else _logs.value + paginated.data
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadNextPage() {
        if (currentPage < lastPage) { currentPage++; load(reset = false) }
    }

    fun clearError() { _error.value = null }
}

// ─── Profile / Data Diri ──────────────────────────────────────────────────────

class ProfileViewModel : ViewModel() {
    private val repo = ProfileRepository()

    private val _employee = MutableStateFlow<EmployeeDetailDto?>(null)
    val employee: StateFlow<EmployeeDetailDto?> = _employee.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Quick read from TokenManager for immediate display before API resolves.
    val cachedName: String get() = TokenManager.userName ?: ""
    val cachedEmail: String get() = TokenManager.userEmail ?: ""

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            // Primary path: /v1/me/profile returns own employee data
            // without requiring module.employees entitlement.
            repo.getMyProfile()
                .onSuccess { emp ->
                    if (emp != null) {
                        _employee.value = emp
                        if (!emp.id.isNullOrBlank()) TokenManager.employeeId = emp.id
                    } else {
                        // No employee record linked to this user account.
                        _error.value = "Profil karyawan belum dikonfigurasi. Hubungi admin HR."
                    }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}

// ─── Cuti (Leave) ────────────────────────────────────────────────────────────

class CutiViewModel : ViewModel() {
    private val repo = LeaveRepository()

    private val _applications = MutableStateFlow<List<LeaveApplicationDto>>(emptyList())
    val applications: StateFlow<List<LeaveApplicationDto>> = _applications.asStateFlow()

    private val _balances = MutableStateFlow<List<LeaveBalanceDto>>(emptyList())
    val balances: StateFlow<List<LeaveBalanceDto>> = _balances.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _submitResult = MutableStateFlow<Result<LeaveApplicationDto>?>(null)
    val submitResult: StateFlow<Result<LeaveApplicationDto>?> = _submitResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getLeaveApplications()
                .onSuccess { _applications.value = it.data }
                .onFailure { _error.value = it.message }
            repo.getLeaveBalances()
                .onSuccess { _balances.value = it }
            _isLoading.value = false
        }
    }

    fun submit(leaveType: String, startDate: String, endDate: String, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _submitResult.value = repo.submitLeave(
                SubmitLeaveApiRequest(leaveType, startDate, endDate, reason)
            )
            if (_submitResult.value?.isSuccess == true) load()
            _isLoading.value = false
        }
    }

    fun cancel(id: String) {
        viewModelScope.launch {
            repo.cancelLeave(id)
                .onSuccess { load() }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearSubmitResult() { _submitResult.value = null }
    fun clearError() { _error.value = null }
}

// ─── Lembur (Overtime) ───────────────────────────────────────────────────────

class LemburViewModel : ViewModel() {
    private val repo = LeaveRepository()

    private val _requests = MutableStateFlow<List<OvertimeRequestDto>>(emptyList())
    val requests: StateFlow<List<OvertimeRequestDto>> = _requests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _submitResult = MutableStateFlow<Result<OvertimeRequestDto>?>(null)
    val submitResult: StateFlow<Result<OvertimeRequestDto>?> = _submitResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getOvertimeRequests()
                .onSuccess { _requests.value = it.data }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun submit(date: String, startTime: String, endTime: String, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _submitResult.value = repo.submitOvertime(
                SubmitOvertimeApiRequest(date, startTime, endTime, reason)
            )
            if (_submitResult.value?.isSuccess == true) load()
            _isLoading.value = false
        }
    }

    fun cancel(id: String) {
        viewModelScope.launch {
            repo.cancelOvertime(id)
                .onSuccess { load() }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearSubmitResult() { _submitResult.value = null }
    fun clearError() { _error.value = null }
}

// ─── Reimbursement ───────────────────────────────────────────────────────────

class ReimbursementViewModel : ViewModel() {
    private val repo = ReimbursementRepository()

    private val _claims = MutableStateFlow<List<ReimbursementClaimDto>>(emptyList())
    val claims: StateFlow<List<ReimbursementClaimDto>> = _claims.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _submitResult = MutableStateFlow<Result<ReimbursementClaimDto>?>(null)
    val submitResult: StateFlow<Result<ReimbursementClaimDto>?> = _submitResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getReimbursements()
                .onSuccess { _claims.value = it.data }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun submit(title: String, category: String, amount: Long, date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _submitResult.value = repo.submitReimbursement(
                title = title,
                item = ReimbursementItemRequest(
                    description = title,
                    amount = amount,
                    date = date,
                    category = category,
                ),
            )
            if (_submitResult.value?.isSuccess == true) load()
            _isLoading.value = false
        }
    }

    fun clearSubmitResult() { _submitResult.value = null }
    fun clearError() { _error.value = null }
}

// ─── Slip Gaji (Payroll) ──────────────────────────────────────────────────────

class SlipGajiViewModel : ViewModel() {
    private val repo = PayrollRepository()

    private val _slips = MutableStateFlow<List<PayrollSlipDto>>(emptyList())
    val slips: StateFlow<List<PayrollSlipDto>> = _slips.asStateFlow()

    private val _selectedDetail = MutableStateFlow<PayrollSlipDetailDto?>(null)
    val selectedDetail: StateFlow<PayrollSlipDetailDto?> = _selectedDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getSlips()
                .onSuccess { _slips.value = it.data }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getSlipDetail(id)
                .onSuccess { _selectedDetail.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearDetail() { _selectedDetail.value = null }
    fun clearError() { _error.value = null }
}

// ─── Pengumuman (Announcements) ───────────────────────────────────────────────

class PengumumanViewModel : ViewModel() {
    private val repo = AnnouncementRepository()

    private val _announcements = MutableStateFlow<List<AnnouncementDto>>(emptyList())
    val announcements: StateFlow<List<AnnouncementDto>> = _announcements.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getAnnouncements()
                .onSuccess { _announcements.value = it.data }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}

// ─── Notifikasi ───────────────────────────────────────────────────────────────

class NotifikasiViewModel : ViewModel() {
    private val repo = NotificationRepository()

    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val notifications: StateFlow<List<NotificationDto>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getNotifications()
                .onSuccess { _notifications.value = it.data }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            repo.markRead(id).onSuccess {
                _notifications.value = _notifications.value.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repo.markAllRead().onSuccess {
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            }.onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
}

// ─── Kalender ─────────────────────────────────────────────────────────────────

class KalenderViewModel : ViewModel() {
    private val repo = CalendarRepository()

    private val _holidays = MutableStateFlow<List<HolidayDto>>(emptyList())
    val holidays: StateFlow<List<HolidayDto>> = _holidays.asStateFlow()

    private val _shiftAssignments = MutableStateFlow<List<ShiftAssignmentDto>>(emptyList())
    val shiftAssignments: StateFlow<List<ShiftAssignmentDto>> = _shiftAssignments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load(year: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getHolidays(year)
                .onSuccess { _holidays.value = it }
                .onFailure { _error.value = it.message }
            // Load shift assignments for the current month ±1.
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -1); val from = sdf.format(cal.time)
            cal.add(Calendar.MONTH, 2);  val to   = sdf.format(cal.time)
            repo.getMyShiftAssignments(from, to)
                .onSuccess { _shiftAssignments.value = it }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}

// ─── Approvals ────────────────────────────────────────────────────────────────

class ApprovalViewModel : ViewModel() {
    private val repo = ApprovalRepository()

    private val _approvals = MutableStateFlow<List<ApprovalDto>>(emptyList())
    val approvals: StateFlow<List<ApprovalDto>> = _approvals.asStateFlow()

    private val _selectedDetail = MutableStateFlow<ApprovalDetailDto?>(null)
    val selectedDetail: StateFlow<ApprovalDetailDto?> = _selectedDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _actionResult = MutableStateFlow<Result<Unit>?>(null)
    val actionResult: StateFlow<Result<Unit>?> = _actionResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load(status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getApprovals(status = status)
                .onSuccess { _approvals.value = it.data }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getApprovalDetail(id)
                .onSuccess { _selectedDetail.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun approve(id: String, notes: String? = null) {
        viewModelScope.launch {
            _actionResult.value = repo.approve(id, notes)
            if (_actionResult.value?.isSuccess == true) load()
        }
    }

    fun reject(id: String, notes: String? = null) {
        viewModelScope.launch {
            _actionResult.value = repo.reject(id, notes)
            if (_actionResult.value?.isSuccess == true) load()
        }
    }

    fun clearDetail() { _selectedDetail.value = null }
    fun clearActionResult() { _actionResult.value = null }
    fun clearError() { _error.value = null }
}

// ─── Performance ─────────────────────────────────────────────────────────────

class PerformanceViewModel : ViewModel() {
    private val repo = PerformanceRepository()

    private val _cycles = MutableStateFlow<List<PerformanceCycleDto>>(emptyList())
    val cycles: StateFlow<List<PerformanceCycleDto>> = _cycles.asStateFlow()

    private val _selectedDetail = MutableStateFlow<PerformanceCycleDetailDto?>(null)
    val selectedDetail: StateFlow<PerformanceCycleDetailDto?> = _selectedDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _submitResult = MutableStateFlow<Result<Unit>?>(null)
    val submitResult: StateFlow<Result<Unit>?> = _submitResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getCycles()
                .onSuccess { _cycles.value = it.data }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getCycleDetail(id)
                .onSuccess { _selectedDetail.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun submitReview(cycleId: String, score: Double, notes: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _submitResult.value = repo.submitReview(SubmitPerformanceReviewRequest(cycleId, score, notes))
            if (_submitResult.value?.isSuccess == true) loadDetail(cycleId)
            _isLoading.value = false
        }
    }

    fun clearDetail() { _selectedDetail.value = null }
    fun clearSubmitResult() { _submitResult.value = null }
    fun clearError() { _error.value = null }
}

// ─── Ubah Password ────────────────────────────────────────────────────────────

class UbahPasswordViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _result = MutableStateFlow<Result<Unit>?>(null)
    val result: StateFlow<Result<Unit>?> = _result.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _result.value = repo.changePassword(currentPassword, newPassword)
            _isLoading.value = false
        }
    }

    fun clearResult() { _result.value = null }
}

// ─── Dokumen ──────────────────────────────────────────────────────────────────

class DokumenViewModel : ViewModel() {
    private val repo = DocumentRepository()

    private val _documents = MutableStateFlow<List<DocumentDto>>(emptyList())
    val documents: StateFlow<List<DocumentDto>> = _documents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getMyDocuments()
                .onSuccess { _documents.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}

// ─── Struktur Org ─────────────────────────────────────────────────────────────

class StrukturOrgViewModel : ViewModel() {
    private val repo = OrgStructureRepository()

    private val _departments = MutableStateFlow<List<OrgDepartmentDto>>(emptyList())
    val departments: StateFlow<List<OrgDepartmentDto>> = _departments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getOrgStructure()
                .onSuccess { _departments.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}

// ─── FAQ ──────────────────────────────────────────────────────────────────────

class FaqViewModel : ViewModel() {
    private val repo = FaqRepository()

    private val _items = MutableStateFlow<List<FaqItemDto>>(emptyList())
    val items: StateFlow<List<FaqItemDto>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getFaq()
                .onSuccess { _items.value = it }
                .onFailure {
                    // Fall back to hardcoded FAQ if API unavailable.
                    if (_items.value.isEmpty()) _items.value = FaqRepository.hardcodedFallback()
                }
            _isLoading.value = false
        }
    }
}

// ─── Aset ─────────────────────────────────────────────────────────────────────

class AssetViewModel : ViewModel() {
    private val repo = AssetRepository()

    private val _assignments = MutableStateFlow<List<AssetAssignmentDto>>(emptyList())
    val assignments: StateFlow<List<AssetAssignmentDto>> = _assignments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load(status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getMyAssets(status)
                .onSuccess { _assignments.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
