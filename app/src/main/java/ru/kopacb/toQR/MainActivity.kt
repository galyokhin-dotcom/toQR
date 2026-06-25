package ru.kopacb.toQR

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.widget.TextView
import androidx.core.content.FileProvider
import com.google.zxing.EncodeHintType
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap

class MainActivity : AppCompatActivity() {

    // Variables for imageview, edittext,
    // button, bitmap and encoder.
    private lateinit var qrCodeIV: ImageView
    private lateinit var dataEdt: EditText
    private lateinit var generateQrBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initializing all variables.
        qrCodeIV = findViewById(R.id.view_for_qr)
        dataEdt = findViewById(R.id.realNiggaUserText)
        generateQrBtn = findViewById(R.id.button2)
        val shareButton = findViewById<Button>(R.id.button3)

        realNiggaUserText = findViewById(R.id.realNiggaUserText)

        handleIncomingIntent(intent)
//        shareButton.setOnClickListener {
//            shareQrCode()
//                }
//
//        shareButton.setOnTouchListener { view, motionEvent ->
//            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
//                Toast.makeText(this, "Кнопка зажата!", Toast.LENGTH_LONG).show()
//                // Если нужно предотвратить срабатывание onClick, верните true
//                false
//            } else {
//                false
//            }
//        }
        shareButton.setOnLongClickListener {
            Toast.makeText(this, "\uD83C\uDF46Не стыдись и поделись!\uD83C\uDF46", Toast.LENGTH_SHORT).show()

            true // true, потому что это уже отдельный тип события
        }

        shareButton.setOnClickListener {
            shareQrCode()
        }



        // Initializing onclick listener for button.
        generateQrBtn.setOnClickListener {
            if (TextUtils.isEmpty(
                    dataEdt.getText().toString()
                )
            ) {

                // If the edittext inputs are empty
                // then execute this method showing
                // a toast message.
                Toast.makeText(
                    this@MainActivity,
                    "\uD83D\uDC1F",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                generateQRCode(
                    dataEdt.getText().toString()
                )
            }
        }
    }

    private fun generateQRCode(text: String) {
        val barcodeEncoder = BarcodeEncoder()
        try {
            // 1. Создаем настройки кодирования и указываем UTF-8
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

            // 2. Передаем hints последним аргументом в MultiFormatWriter
            val multiFormatWriter = com.google.zxing.MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(
                text,
                BarcodeFormat.QR_CODE,
                350,
                350,
                hints // Передаем настройки сюда
            )

            // 3. Создаем Bitmap из матрицы
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)

            // 4. Устанавливаем в ImageView
            qrCodeIV.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка генерации", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareQrCode() {
        // 1. Получаем Bitmap из вашего ImageView
        val drawable = qrCodeIV.drawable as? BitmapDrawable
        if (drawable == null) {
            Toast.makeText(this, "Сначала сгенерируйте QR-код", Toast.LENGTH_SHORT).show()
            return
        }
        val bitmap = drawable.bitmap

        try {
            // 2. Создаем папку в кэше и сохраняем туда файл
                val cachePath = File(cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "qr_code.png")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                // 3. Получаем безопасный Uri файла через FileProvider
                val contentUri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )

                // 4. Запускаем окно "Поделиться"
                if (contentUri != null) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Разрешаем другим читать файл
                        setDataAndType(contentUri, contentResolver.getType(contentUri))
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        type = "image/png"
                    }
                    startActivity(Intent.createChooser(shareIntent, "Поделиться QR-кодом через:"))
                }
        } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка при отправке", Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var realNiggaUserText: TextView



    // Если пользователь открыл приложение через «Поделиться», Intent придёт сюда
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        val action = intent.action
        val type = intent.type

        when {
            Intent.ACTION_SEND == action && type?.startsWith("text/") == true -> {
                handleSendText(intent)
            }
            Intent.ACTION_SEND_MULTIPLE == action -> {
                handleSendMultiple(intent)
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!text.isNullOrBlank()) {
            realNiggaUserText.text = text
            Toast.makeText(this, "Текст получен", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Пустой текст", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSendMultiple(intent: Intent) {
        val clipData = intent.clipData
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)

        if (!text.isNullOrBlank()) {
            realNiggaUserText.text = text
        } else if (clipData != null) {
            realNiggaUserText.text = "Получено вложений: ${clipData.itemCount}"
        } else {
            realNiggaUserText.text = ""
        }
        Toast.makeText(this, "Обработано несколько элементов", Toast.LENGTH_SHORT).show()
    }
}








