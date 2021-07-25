package com.example.zettlecaster.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.example.zettlecaster.databinding.ActivityLoginBinding
import com.example.zettlecaster.databinding.LinksPopupBinding
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ZettleActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_PERMISSION = 42
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        }

        var binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = binding.title
        val contents = binding.contents
        val create = binding.createButton
        val addLink = binding.addLink

        title.requestFocus()

        title.afterTextChanged {
            create.isEnabled = title.text.isNotEmpty()
        }

        var parent = this

        contents.apply {
            create.setOnClickListener {

                var filename = createZettle(title.text.toString(), contents.text.toString())
                Toast.makeText(applicationContext, "Created zettle: $filename", Toast.LENGTH_SHORT).show()
                title.setText("")
                contents.setText("")
            }

            addLink.setOnClickListener {

                var popupView = LinksPopupBinding.inflate(layoutInflater)

                // create the popup window
                var width = LinearLayout.LayoutParams.MATCH_PARENT
                var height = LinearLayout.LayoutParams.WRAP_CONTENT
                var focusable = true
                var popupWindow =  PopupWindow(popupView.root, width, height, focusable);

                popupWindow.showAtLocation(parent.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

                var path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                var buttons = Vector<Button>()
                File("$path/Obsidian/Obsidian/").walk().forEach {
                    if (it.isFile) {
                        var button = Button(parent)
                        var name = it.nameWithoutExtension
                        button.transformationMethod = null
                        button.text = name
                        button.setOnClickListener {
                            popupWindow.dismiss()
                            contents.text.insert(contents.selectionStart,"[[$name]]" )
                            contents.requestFocus()
                        }
                        popupView.linearLayout.addView(button)
                        buttons.add(button)
                    }
                }

                popupView.root.setOnTouchListener { _, _ ->
                    popupWindow.dismiss()
                    true
                }

                popupView.searchBar.requestFocus()

                popupView.searchBar.doOnTextChanged { text, _, _, _ ->
                    var t = text ?: ""
                    var lowert = t.toString().lowercase()
                    buttons.forEach {
                         it.visibility = if (it.text.toString().lowercase().contains(lowert)) { View.VISIBLE} else {View.GONE}
                    }
                }
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

    private fun createZettle(title: String, content: String) : String {

        var now = LocalDateTime.now()

        var nowFmt = now.format (DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"))

        var fileContents = """---
type: zettle
creation_time: $nowFmt
---
# $title            
$content
"""

        var path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        var zettleId = now.format(DateTimeFormatter.ofPattern("yyMMdd_")) + (now.hour * 3600 + now.minute * 60 + now.second).toString(36)
        var filename = "${zettleId}-$title.md"
        File("$path/Obsidian/Obsidian/Zettle", filename).writeText(fileContents)

        return filename
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}