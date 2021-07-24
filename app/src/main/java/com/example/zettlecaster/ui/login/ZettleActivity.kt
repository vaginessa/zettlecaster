package com.example.zettlecaster.ui.login

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.zettlecaster.databinding.ActivityLoginBinding


class ZettleActivity : AppCompatActivity() {

    private lateinit var zettleViewModel: ZettleViewModel
    private lateinit var binding: ActivityLoginBinding

    companion object {
        const val REQUEST_PERMISSION = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = binding.title
        val contents = binding.contents
        val create = binding.createButton

        zettleViewModel = ZettleViewModel()

        zettleViewModel.loginFormState.observe(this@ZettleActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            create.isEnabled = loginState.isDataValid
        })

        title.afterTextChanged {
            zettleViewModel.zettleDataChanged(
                    title.text.toString(),
                    contents.text.toString()
            )
        }

        contents.apply {
            create.setOnClickListener {

                var filename = zettleViewModel.createZettle(title.text.toString(), contents.text.toString())
                Toast.makeText(applicationContext, "Created zettle: $filename", Toast.LENGTH_SHORT).show()
                title.setText("")
                contents.setText("")
            }
        }
    }

    private fun showZettleFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION -> if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Could not get permissions to write to external storage",Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}