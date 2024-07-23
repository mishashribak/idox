package com.knowledgeways.idocs.utils

import android.content.Context
import android.content.res.Configuration
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.model.theme.FontSize
import com.knowledgeways.idocs.network.model.theme.ThemeTextStyle
import java.util.*

object ResUtils {

    fun getLoginBackground(context: Context, orientation: Int) : String{
       return if (orientation == Configuration.ORIENTATION_LANDSCAPE) getLoginBackgroundLandscape(context)
       else getLoginBackgroundPortrait(context)
    }

    private fun getLoginBackgroundPortrait(context: Context): String{
        val imageName = context.getString(R.string.login_page_background_portrait)
        return """${
            FileUtils.getImageDirectory(
                context,
                CommonUtils.getDefaultThemeId()
            )
        }$imageName"""
    }

    private fun getLoginBackgroundLandscape(context: Context) : String{
        val imageName = context.getString(R.string.login_page_background_landscape)
        return """${
            FileUtils.getImageDirectory(
                context,
                CommonUtils.getDefaultThemeId()
            )
        }$imageName"""
    }

    fun getLoginButtonImage(context: Context) : String{
        val imageName = context.getString(R.string.login_button_resource)
       return """${
           FileUtils.getImageDirectory(
               context,
               CommonUtils.getDefaultThemeId()
           )
       }$imageName"""
    }

    fun getNormalImageFromTheme(context: Context, imageName: String) : String{
        return """${
            FileUtils.getImageDirectory(
                context,
                CommonUtils.getDefaultThemeId()
            )
        }$imageName"""
    }

    fun getIconFromTheme(context: Context, iconName: String) : String{
        return """${
            FileUtils.getIconDirectory(
                context,
                CommonUtils.getDefaultThemeId()
            )
        }$iconName"""
    }

    fun getBoxIconName(name: String, selected: Boolean): String{
        return "$name${if (selected) AppConstants.BOX_ICON_SUFFIX_SELECTED else AppConstants.BOX_ICON_SUFFIX_DESELECTED}${AppConstants.FILE_NAME_PNG}"
    }

    fun getBoxIconNam(context: Context, name: String, selected: Boolean): String{
        return getIconFromTheme(context, getBoxIconName(name, selected))
    }

    fun getToolbarBackgroundImage(context: Context, imageName: String): String{
        return """${
            FileUtils.getImageDirectory(
                context,
                CommonUtils.getDefaultThemeId()
            )
        }$imageName"""
    }

    fun checkCurrentLocale() {
        if (Locale.getDefault().displayLanguage == "العربية" ||
            Locale.getDefault().displayLanguage.equals("arabic", true)
        ) {
            PreferenceManager.userLanguage = 2
        } else {
            PreferenceManager.userLanguage = 1
        }
    }

    fun getTextSize(themeTextStyle: ThemeTextStyle) : Float{
        val fontValue =  if (PreferenceManager.userLanguage == 1) themeTextStyle.fontSize?.en ?: 0
        else  themeTextStyle.fontSize?.ar ?: 0

        return fontValue.toFloat()
    }


    fun getTextSize(fontSize: FontSize) : Float{
        val fontValue =  if (PreferenceManager.userLanguage == 1) fontSize.en ?: 0
        else  fontSize.ar ?: 0

        return fontValue.toFloat()
    }

    fun getEdittextStyle(isBold: Boolean) : Int{
        return if (isBold) R.style.EditText_Bold else R.style.EditText_Normal
    }

    fun getTextViewStyle(isBold: Boolean) : Int{
        return if (isBold) R.style.TextFont_Bold else R.style.TextFont_Regular
    }

    fun getPriorityIcon(context: Context, priorityValue: String): String{
        return getIconFromTheme(context, getPriorityIconName(priorityValue))
    }

    fun getConfidentialIcon(context: Context, priorityValue: String): String{
        return getIconFromTheme(context, getConfidentialIconName(priorityValue))
    }

    fun getPriorityIconName(priorityValue: String): String{
        return "priority-${priorityValue}@2x${AppConstants.FILE_NAME_PNG}"
    }

    fun getConfidentialIconName(confidentialValue: String): String{
        return "confidential-${confidentialValue}@2x${AppConstants.FILE_NAME_PNG}"
    }

    fun getToolbarIcon(context: Context, iconName: String): String{
        return getIconFromTheme(context, iconName)
    }
}