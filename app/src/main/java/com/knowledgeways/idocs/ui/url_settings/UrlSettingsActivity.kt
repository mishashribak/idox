package com.knowledgeways.idocs.ui.url_settings

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.webkit.URLUtil
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseActivity
import com.knowledgeways.idocs.databinding.ActivityUrlSettingsBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.utils.KeyboardUtils
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


class UrlSettingsActivity: BaseActivity<ActivityUrlSettingsBinding, UrlSettingsViewModel>(),
    HasAndroidInjector {

    override val layoutId: Int
        get() = R.layout.activity_url_settings

    override val viewModel: UrlSettingsViewModel
        get() {
            return getViewModel(UrlSettingsViewModel::class.java)
        }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector() = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUI()
    }

    private fun initUI(){
        viewDataBinding?.apply {
            edittextServerUrl.setText(PreferenceManager.baseUrl)

            tvDone.setOnClickListener {
                var url =  edittextServerUrl.text.toString().trim()
                if (url[url.length-1] != '/') url +=  "/"

                if (Patterns.WEB_URL.matcher(url).matches()){
                    PreferenceManager.baseUrl = url
                    val returnIntent = Intent()
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }else{
                    Toast.makeText(this@UrlSettingsActivity, "This URL is not valid",
                        LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        KeyboardUtils.hideKeyboard(this)
        val returnIntent = Intent()
        setResult(RESULT_CANCELED, returnIntent)
        finish()
    }
}