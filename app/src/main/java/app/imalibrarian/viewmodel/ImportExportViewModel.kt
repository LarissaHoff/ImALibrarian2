package app.imalibrarian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.imalibrarian.domain.usecase.ExportBooksUseCase
import app.imalibrarian.domain.usecase.ImportBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportExportUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportData: String = "",
    val importedCount: Int = 0,
    val errorMessage: String? = null,
    val exportComplete: Boolean = false,
    val importComplete: Boolean = false
)

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val exportBooksUseCase: ExportBooksUseCase,
    private val importBooksUseCase: ImportBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportExportUiState())
    val uiState: StateFlow<ImportExportUiState> = _uiState.asStateFlow()

    fun exportToJson() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val json = exportBooksUseCase.exportToJson()
                _uiState.value = _uiState.value.copy(
                    exportData = json,
                    isExporting = false,
                    exportComplete = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val csv = exportBooksUseCase.exportToCsv()
                _uiState.value = _uiState.value.copy(
                    exportData = csv,
                    isExporting = false,
                    exportComplete = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun importFromJson(json: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val count = importBooksUseCase.importFromJson(json)
                _uiState.value = _uiState.value.copy(
                    importedCount = count,
                    isImporting = false,
                    importComplete = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun importFromCsv(csv: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val count = importBooksUseCase.importFromCsv(csv)
                _uiState.value = _uiState.value.copy(
                    importedCount = count,
                    isImporting = false,
                    importComplete = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun clearState() {
        _uiState.value = ImportExportUiState()
    }
}