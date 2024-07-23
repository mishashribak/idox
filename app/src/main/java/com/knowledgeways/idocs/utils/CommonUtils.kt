package com.knowledgeways.idocs.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.network.model.Box
import com.knowledgeways.idocs.network.model.DrawerItemColor
import com.knowledgeways.idocs.network.model.theme.Theme
import java.io.Serializable


object CommonUtils {

    fun getBackgroundColor(imageView: ImageView): String {
        return if (imageView.drawable != null) {
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val pixel = bitmap.getPixel(0, 0)
            parseDecimalColor(pixel)
        } else ""
    }

    fun getBackgroundColor(bitmap: Bitmap): String {
        val pixel = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        return parseDecimalColor(pixel)
    }

    private fun parseDecimalColor(decimalColor: Int): String {
        val r = Integer.toHexString(decimalColor shr 16 and 0xFF)
        val g = Integer.toHexString(decimalColor shr 8 and 0xFF)
        val b = Integer.toHexString(decimalColor shr 0 and 0xFF)

        return "#${if (r.length == 1) "0$r" else r}${if (g.length == 1) "0$g" else g}${if (b.length == 1) "0$b" else b}"
    }

    fun validateInfo(context: Context, username: String, password: String): String {
        return if (username.isNotEmpty() && password.isNotEmpty() && password.length > 4) {
            ""
        } else if (username.isEmpty()) {
            context.resources.getString(R.string.str_enter_username)
        } else if (password.isEmpty()) {
            context.resources.getString(R.string.str_enter_password)
        } else {
            context.resources.getString(R.string.str_invalid_password)
        }
    }

    fun getDefaultTheme(): Theme?{
        val themeList = ConverterUtils.getThemeList() ?: ArrayList()
        return if (themeList.isEmpty()){
            null
        }else{
            var defaultTheme: Theme ?= null
            for (theme in themeList){
                if (theme.default == true){
                    defaultTheme = theme
                    break
                }
            }
            defaultTheme
        }
    }

    fun getDefaultThemeId(): String{
        return getDefaultTheme()?.themeId ?: ""
    }

    fun trimSessionId(string: String): String{
        return string.replace("Path=/kw-ecm; HttpOnly", "")
    }

    fun getRememberMeString(string: String): String{
        val separated: List<String> = string.split("Max-Age")
        if (separated.isEmpty()) return ""
        return separated.first().replace(";", "").replace(" ", "")
    }

    private fun getColor(colorString : String?) : Int{
        if (colorString == null) return 0
        return Color.parseColor(colorString)
    }

    fun getBoxTextColor(box: Box, selected: Boolean): Int{
        return if (selected) getColor(box.selectedTitleColor) else getColor(box.titleColor)
    }

    fun getColorFromImageView(context: Context, imageView: ImageView, imageName: String, onImageReady :(colorString: String) -> Unit){
        GlideApp.with(context).asBitmap()
            .load(ResUtils.getNormalImageFromTheme(context, imageName))
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap>?,
                    isFirstResource: Boolean,
                ): Boolean {

                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    onImageReady(getBackgroundColor(resource!!))
                    return false
                }
            }).into(imageView)
    }

    fun loadIconToImageView(context: Context, iconURL: String, view:ImageView?){
        if (view != null) GlideApp.with(context)
            .load(iconURL)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view)
    }

    fun isColorItemEmpty(drawerItemColor: DrawerItemColor): Boolean =
        (drawerItemColor.colorDeselected == "" || drawerItemColor.colorSelected == "")

    fun isColorListFilled(colorData: List<DrawerItemColor>): Boolean{
        var allColorFilled = true
        if (colorData.size != 3)  return false
        colorData.forEach {
            if (isColorItemEmpty(it)) {
                allColorFilled = false
            }
        }

        return allColorFilled
    }

    private var isNetworkConnected = true

    fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O) {
                if (connectivityManager.activeNetwork == null) {
                    isNetworkConnected = false
                }

                val networkCapabilities = connectivityManager.activeNetwork ?: return false

                if (connectivityManager.getNetworkCapabilities(networkCapabilities) == null) {
                    isNetworkConnected = false
                }

                val actNw =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

                isNetworkConnected = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }

                return isNetworkConnected
            } else {
                return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isAvailable &&
                        connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting &&
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.state == NetworkInfo.State.CONNECTED
                        && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)?.state == NetworkInfo.State.CONNECTED
            }
        }

    fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
    }

/*    @Throws(JsonMappingException::class, JsonProcessingException::class)
    fun getKeysInJsonUsingJsonNodeFieldNames(json: String?, mapper: ObjectMapper): List<String>? {
        val keys: MutableList<String> = ArrayList()
        val jsonNode: JsonNode = mapper.readTree(json)
        val iterator: Iterator<String> = jsonNode.fieldNames()
        iterator.forEachRemaining { e: String ->
            keys.add(
                e
            )
        }
        return keys
    }*/

    fun reformatUrl(url: String) : String{
        return if (url.substring(url.length - 1)== "/") url else "$url/"
    }
}
