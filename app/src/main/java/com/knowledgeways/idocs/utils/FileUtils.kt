package com.knowledgeways.idocs.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.knowledgeways.idocs.network.model.theme.Theme
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI


object FileUtils {

    fun getZipFileName(mContext: Context, themeID: String, fileType: Int): String {
        val nameAddition =
            if (fileType == AppConstants.FILE_TYPE_IMAGE) AppConstants.FILE_NAME_IMAGE_ZIP else AppConstants.FILE_NAME_ICON_ZIP

        return """${mContext.getExternalFilesDir(null)?.absolutePath}${File.separator}$themeID$nameAddition"""
    }

    fun getPDFFolderURL(mContext: Context): String {
        return """${mContext.getExternalFilesDir(null)?.absolutePath}${File.separator}pdf/"""
    }

    fun getPDFFileName(
        mContext: Context,
        documentId: String,
        transferId: String,
    ): String {
        return """${mContext.getExternalFilesDir(null)?.absolutePath}${File.separator}pdf/doc-${documentId}-${transferId}.pdf"""
    }

    fun getTempFileName(
        mContext: Context,
        documentId: String,
        transferId: String,
    ): String {
        return """${mContext.getExternalFilesDir(null)?.absolutePath}${File.separator}pdf/doc-${documentId}-${transferId}-temp.pdf"""
    }

    fun getUnzipFileDirectory(mContext: Context, themeID: String, fileType: Int): String {
        val nameAddition =
            if (fileType == AppConstants.FILE_TYPE_IMAGE) AppConstants.IMAGE_URL else AppConstants.ICON_URL

        return """${mContext.getExternalFilesDir(null)?.absolutePath}${File.separator}$themeID$nameAddition"""
    }

    fun getImageDirectory(mContext: Context, themeID: String): String {
        return getUnzipFileDirectory(mContext, themeID, AppConstants.FILE_TYPE_IMAGE)
    }

    fun getIconDirectory(mContext: Context, themeID: String): String {
        return getUnzipFileDirectory(mContext, themeID, AppConstants.FILE_TYPE_ICON)
    }

    fun writeResponseBodyToDisk(
        mContext: Context,
        body: ResponseBody,
        themeID: String,
        fileType: Int,
        onFinished: () -> Unit
    ): Boolean {

        return try {
            //generate file name
            val fileToSave =
                File(getZipFileName(mContext, themeID, fileType))
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                inputStream = body.byteStream()
                outputStream = FileOutputStream(fileToSave)
                var count = 0
                while (true) {
                    val read = inputStream.read(fileReader)
                    count++
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                Log.d("File Save Result", e.message!!)
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
                if (fileType == AppConstants.FILE_TYPE_IMAGE) onFinished()
            }
        } catch (e: IOException) {
            Log.d("File Save Result", e.message!!)
            false
        }
    }

    fun writePDFFileToDisk(
        mContext: Context,
        body: ResponseBody,
        documentId: String,
        transferId: String,
        onFinished: (documentURI: Uri) -> Unit
    ): Boolean {

        return try {
            //generate file name
            val fileToSave =
                File(getPDFFileName(mContext, documentId, transferId))
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                inputStream = body.byteStream()
                outputStream = FileOutputStream(fileToSave)
                var count = 0
                while (true) {
                    val read = inputStream.read(fileReader)
                    count++
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                Log.d("File Save Result", e.message!!)
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
                onFinished(Uri.fromFile(fileToSave))
            }
        } catch (e: IOException) {
            Log.d("File Save Result", e.message!!)
            false
        }
    }

    fun unzip(zipFileName: String, destinationFolder: String) {
        try {
            val zipFile = ZipFile(zipFileName)
            zipFile.extractAll(destinationFolder)
        } catch (e: ZipException) {
            e.printStackTrace()
        }
    }

    fun isFileExisting(
        mContext: Context,
        theme: Theme,
        fileType: Int
    ): Boolean {
        val file =
            File(getZipFileName(mContext, theme.themeId ?: "", fileType))
        return (file.exists())
    }

    fun isPDFExisting(
        mContext: Context,
        documentId: String,
        transferId: String,
    ): Boolean{
        val file = File(getPDFFileName(mContext, documentId, transferId))
        return file.exists()
    }

    fun deleteAllPDF(mContext: Context){
        val fileImage =
            File("""${mContext.getExternalFilesDir(null)?.absolutePath}${File.separator}pdf/""")
        deleteRecursive(fileImage)

        createPDFFolder(mContext)
    }

    fun isThemeExisting(
        mContext: Context,
        theme: Theme,
    ): Boolean {
        return (isFileExisting(mContext, theme, AppConstants.FILE_TYPE_IMAGE)) && (isFileExisting(
            mContext,
            theme,
            AppConstants.FILE_TYPE_ICON
        ))
    }

    fun deleteTheme(
        mContext: Context,
        theme: Theme,
    ) {
        val fileImage =
            File(getZipFileName(mContext, theme.themeId ?: "", AppConstants.FILE_TYPE_IMAGE))

        val fileIcon =
            File(getZipFileName(mContext, theme.themeId ?: "", AppConstants.FILE_TYPE_ICON))

        deleteRecursive(fileImage)
        deleteRecursive(fileIcon)
    }

    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()!!) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }

    fun createPDFFolder(mContext: Context){
        val file = File(getPDFFolderURL(mContext))
        if (!file.exists()){
            file.mkdirs()
        }
    }
}
