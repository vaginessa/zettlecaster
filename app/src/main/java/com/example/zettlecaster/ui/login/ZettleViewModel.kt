package com.example.zettlecaster.ui.login

import android.content.Context
import android.os.Environment
import androidx.core.os.EnvironmentCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zettlecaster.data.LoginRepository
import com.example.zettlecaster.data.Result
import java.io.File;
import com.example.zettlecaster.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LoginFormState(val isDataValid: Boolean = false)

data class ZettleResult(
    val success: Boolean? = null,
    val error: Int? = null
)

class ZettleViewModel() : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _zettleResult = MutableLiveData<ZettleResult>()
    val zettleResult: LiveData<ZettleResult> = _zettleResult

    fun createZettle(title: String, content: String) : String {

        var now = LocalDateTime.now()

        var nowFmt = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        var fileContents = """
            ---
            type: zettle
            creation_time: $nowFmt
            ---
            # $title
            
            $content
        """.trimIndent()

        var path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        var zettleId = now.format(DateTimeFormatter.ofPattern("yyMMdd_")) + (now.hour * 3600 + now.minute * 60 + now.second).toString(36)
        var filename = "${zettleId}-$title.md"
        File("$path/Obsidian/Obsidian/Zettle", filename).writeText(fileContents)

        _zettleResult.value = ZettleResult(success = true)
        return filename
    }

    fun zettleDataChanged(title: String, contents: String) {
        _loginForm.value = LoginFormState(isDataValid = title.isNotEmpty())
    }
}