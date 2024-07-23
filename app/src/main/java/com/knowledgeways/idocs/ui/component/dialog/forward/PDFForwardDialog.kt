package com.knowledgeways.idocs.ui.component.dialog.forward

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.view.Window
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.material.badge.BadgeDrawable
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutDocumentForwardBinding
import com.knowledgeways.idocs.network.model.DocExternalUser
import com.knowledgeways.idocs.network.model.ExternalUser
import com.knowledgeways.idocs.network.model.user.DocOrganization
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.ui.component.dialog.userNorgdetail.OrganizationDetailDialog
import com.knowledgeways.idocs.ui.component.dialog.userNorgdetail.UserDetailDialog
import com.knowledgeways.idocs.ui.pdf.CustomPDFActivity
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils


class PDFForwardDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null
    lateinit var mContext: Context

    private var badgeOrg: BadgeDrawable? = null
    private var badgeSisterOrg: BadgeDrawable?= null
    private var badgeUser: BadgeDrawable?= null

    lateinit var mBinding: LayoutDocumentForwardBinding

    var docOrgAdapter = DocORGAdapter(false, {org ->
        onDocOrgClicked(org,false)},{org->})

    var docUserAdapter = DocUserAdapter (false, { user ->
        onUserClicked(user, false)
    },{})

    var favoriteOrgAdapter = DocORGAdapter (true,
        { org ->
            onDocOrgClicked(org,true)
        }, {org-> onDeleteFavoriteOrgClicked(org)}
    )

    var favoriteSisterOrgAdapter = DocORGAdapter ( true,
        { org ->
            onDocOrgClicked(org, true)
        },{org -> onDeleteSisterOrgFromFavorite(org)}
    )

    var sisterOrgAdapter = DocORGAdapter( false,
        { org ->
            onDocOrgClicked(org, false)
        }, {}
    )

    var favoriteUserAdapter = DocUserAdapter(true, { user ->
        onUserClicked(user, true)
    },{ user -> onDeleteUserFromFavoriteClicked(user)})

    var selectedOrgAdapter = DocSelectedOrgAdapter { organization ->
        onDeleteSelectedOrgClicked(organization)
    }

    var selectedSisterOrgAdapter = DocSelectedOrgAdapter { organization ->
        onDeleteSelectedSisterOrgClicked(organization)
    }

    var selectedUserAdapter = DocSelectedUserAdapter { organization ->
        onDeleteSelectedUserClicked(organization)
    }

    lateinit var mOnAddSelectedOrgClicked: (org: Organization) -> Unit
    lateinit var mOnAddOrgToFavoriteClicked : (org: Organization) -> Unit

    fun openDialog(
        context: Context,
        imageUrls: List<String>,
        mColor: ColorStateList,
        isQuickForward: Boolean,
        onAddSelectedOrgClicked: (org: Organization) -> Unit,
        onAddOrgToFavoriteClicked: (org: Organization) -> Unit
    ) {
        dialog = Dialog(context)

        mOnAddSelectedOrgClicked = onAddSelectedOrgClicked
        mOnAddOrgToFavoriteClicked = onAddOrgToFavoriteClicked
        mContext = context

        val binding = DataBindingUtil.inflate<LayoutDocumentForwardBinding>(
            LayoutInflater.from(context),
            R.layout.layout_document_forward,
            null,
            false
        )

        mBinding = binding
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)
        isDialogRunning = true
        dialog?.setOnDismissListener {
            isDialogRunning = false
        }

        binding.apply {
            if (com.knowledgeways.idocs.db.PreferenceManager.toolbarColor.isNotEmpty()) {
                val colorStateList =
                    ColorStateList.valueOf(Color.parseColor(com.knowledgeways.idocs.db.PreferenceManager.toolbarColor))
                layoutToolbar?.backgroundTintList = colorStateList
            }
            tvClose?.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvClose?.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle?.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle?.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle?.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            tvClose?.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)

            tvClose?.setOnClickListener {
                dialog?.dismiss()
            }

            tvSend?.setOnClickListener {
                (context as CustomPDFActivity).sendDocument()
            }

            bottomNavBar?.itemTextColor = mColor
            bottomNavBar?.itemIconTintList = mColor

            for (i in 0 until bottomNavBar?.menu!!.size()) {
                val menuItem: MenuItem = bottomNavBar.menu.getItem(i)
                val imageUrl = imageUrls[i]

                // Load the image using Glide
                Glide.with(context)
                    .load(imageUrl)
                    .into(object : CustomTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: com.bumptech.glide.request.transition.Transition<in Drawable?>?
                        ) {
                            menuItem.icon = resource
                        }

                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {
                            // This method can be left empty
                        }
                    })
            }

            bottomNavBar.menu.getItem(2).isVisible = !isQuickForward
            bottomNavBar.menu.getItem(3).isVisible = !isQuickForward

            bottomNavBar.setOnNavigationItemSelectedListener { item -> // Handle the item selection
                when (item.itemId) {
                    R.id.favorite -> {
                        layoutOverlayFavorite?.visibility = View.VISIBLE
                        layoutOverlayEmployees?.visibility = View.GONE
                        layoutOverlayOrganizations?.visibility = View.GONE
                        layoutOverlaySisterOrganizations?.visibility = View.GONE
                    }

                    R.id.organization -> {
                        layoutOverlayFavorite?.visibility = View.GONE
                        layoutOverlayEmployees?.visibility = View.GONE
                        layoutOverlayOrganizations?.visibility = View.VISIBLE
                        layoutOverlaySisterOrganizations?.visibility = View.GONE
                    }

                    R.id.employee -> {
                        layoutOverlayFavorite?.visibility = View.GONE
                        layoutOverlayEmployees?.visibility = VISIBLE
                        layoutOverlayOrganizations?.visibility = View.GONE
                        layoutOverlaySisterOrganizations?.visibility = View.GONE
                    }

                    R.id.sister_organization -> {
                        layoutOverlayFavorite?.visibility = View.GONE
                        layoutOverlayEmployees?.visibility = View.GONE
                        layoutOverlayOrganizations?.visibility = View.GONE
                        layoutOverlaySisterOrganizations?.visibility = VISIBLE
                    }
                }
                // Return true to indicate that the item selection has been handled
                true
            }
            badgeOrg = bottomNavBar.getOrCreateBadge(R.id.organization)
            badgeOrg?.backgroundColor =
                ContextCompat.getColor(context, R.color.color_red)
            badgeOrg?.isVisible = true

            layoutOrganizations?.apply {

                rvSelected.apply {
                    adapter = selectedOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                recyclerView.apply {
                    adapter = docOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }
            }

            layoutFavorite?.apply {
                listEmployee.apply {
                    adapter = favoriteUserAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                listOrg.apply {
                    adapter = favoriteOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                listSisterOrg.apply {
                    adapter = favoriteSisterOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }
            }

            layoutSisterOrganizations?.apply {

                badgeSisterOrg = bottomNavBar.getOrCreateBadge(R.id.sister_organization)
                badgeSisterOrg?.backgroundColor =
                    ContextCompat.getColor(context, R.color.color_red)
                badgeSisterOrg?.isVisible = true

                rvSelected.apply {
                    adapter = selectedSisterOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                recyclerView.apply {
                    adapter = sisterOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }
            }

            layoutEmployees?.apply {

                badgeUser = bottomNavBar.getOrCreateBadge(R.id.employee)
                badgeUser?.backgroundColor =
                    ContextCompat.getColor(context, R.color.color_red)
                badgeUser?.isVisible = true

                rvSelected.apply {
                    adapter = selectedUserAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                recyclerView.apply {
                    adapter = docUserAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }
            }

        }


        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun onDocOrgClicked(org: Organization, isFavorite: Boolean) {
        OrganizationDetailDialog.openDialog(
            mContext,
            isFavorite,
            org)
    }

    fun onUserClicked(user: ExternalUser, isFavorite: Boolean) {
        UserDetailDialog.openDialog(mContext, isFavorite,  user)
    }

    fun setFavoriteOrgDataToView(docOrg: DocOrganization) {
        favoriteOrgAdapter.setItems(docOrg.orgData)
        favoriteOrgAdapter.filterKey = ""
    }

    fun setAllOrgDataToView(docOrg: DocOrganization) {
        docOrgAdapter.setItems(docOrg.orgData)
        docOrgAdapter.filterKey = ""
    }

    fun setFavoriteSisterOrgDataToView(docOrg: DocOrganization) {
        favoriteSisterOrgAdapter.setItems(docOrg.orgData)
        favoriteSisterOrgAdapter.filterKey = ""
    }

    fun setAllSisterOrgDataToView(docOrg: DocOrganization) {
        sisterOrgAdapter.setItems(docOrg.orgData)
        sisterOrgAdapter.filterKey = ""
    }

    fun setFavoriteUserDataToView(docExternalUser: DocExternalUser) {
        favoriteUserAdapter.setItems(docExternalUser.orgData)
        favoriteUserAdapter.filterKey = ""
    }

    fun setAllUserDataToView(docExternalUser: DocExternalUser) {
        docUserAdapter.setItems(docExternalUser.orgData)
        docUserAdapter.filterKey = ""
    }

    fun setSelectedOrgDataToView(orgList: List<Organization>) {
        selectedOrgAdapter.setItems(orgList)
        badgeOrg?.number = orgList.size
        badgeOrg?.isVisible = orgList.isNotEmpty()
    }

    fun setSelectedSisterOrgDataToView(orgList: List<Organization>) {
        selectedSisterOrgAdapter.setItems(orgList)
        badgeSisterOrg?.number = orgList.size
        badgeSisterOrg?.isVisible = orgList.isNotEmpty()
    }

    fun setSelectedUserDataToView(orgList: List<ExternalUser>) {
        selectedUserAdapter.setItems(orgList)
        badgeUser?.number = orgList.size
        badgeUser?.isVisible = orgList.isNotEmpty()
    }

    fun onDeleteFavoriteOrgClicked(org: Organization){
        (mContext as CustomPDFActivity).onDeleteOrgFromFavorite(org)
    }

    fun onDeleteSisterOrgFromFavorite(org: Organization){
        (mContext as CustomPDFActivity).onDeleteSisterOrgFromFavorite(org)
    }

    fun onDeleteSelectedOrgClicked(org: Organization) {
        (mContext as CustomPDFActivity).onDeleteSelectedOrgClicked(org)
    }

    fun onDeleteSelectedSisterOrgClicked(org: Organization) {
        (mContext as CustomPDFActivity).onDeleteSelectedSisterOrgClicked(org)
    }

    fun onDeleteSelectedUserClicked(user: ExternalUser) {
        (mContext as CustomPDFActivity).onDeleteSelectedUserClicked(user)
    }

    fun onDeleteUserFromFavoriteClicked(user: ExternalUser){
        (mContext as CustomPDFActivity).onDeleteUserFromFavorite(user)
    }


    fun dismissDialog() {
        dialog?.dismiss()
        isDialogRunning = false
    }

    fun  deleteOrgElement(org: Organization){
        favoriteOrgAdapter.removeElement(org)
    }

    fun deleteSisterOrgElement(org: Organization){
        favoriteSisterOrgAdapter.removeElement(org)
    }

    fun deleteUserElement(user: ExternalUser){
        favoriteUserAdapter.removeElement(user)
    }

    companion object {
        private var instance: PDFForwardDialog? = null
        private val Instance: PDFForwardDialog
            get() {
                if (instance == null) {
                    instance = PDFForwardDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            imageUrls: List<String>,
            colorStateList: ColorStateList,
            isQuickForward: Boolean,
            onAddSelectedOrgClicked: (org: Organization) -> Unit,
            onAddOrgToFavoriteClicked: (org: Organization) -> Unit
        ) {
            Instance.openDialog(context,
                imageUrls,
                colorStateList,
                isQuickForward,
                { org -> onAddSelectedOrgClicked(org) },
                { org -> onAddOrgToFavoriteClicked(org) })
        }

        fun setFavoriteOrgDataToView(docOrg: DocOrganization) {
            Instance.setFavoriteOrgDataToView(docOrg)
        }

        fun setFavoriteSisterOrgDataToView(sisterOrg: DocOrganization) {
            Instance.setFavoriteSisterOrgDataToView(sisterOrg)
        }

        fun setFavoriteUserDataToView(docExternalUser: DocExternalUser) {
            Instance.setFavoriteUserDataToView(docExternalUser)
        }

        fun setOrgDataToView(docOrg: DocOrganization) {
            Instance.setAllOrgDataToView(docOrg)
        }

        fun setSisterOrgDataToView(sisterOrg: DocOrganization) {
            Instance.setAllSisterOrgDataToView(sisterOrg)
        }

        fun setSelectedOrgDataToView(orgList: List<Organization>) {
            Instance.setSelectedOrgDataToView(orgList)
        }

        fun setSelectedSisterOrgDataToView(orgList: List<Organization>) {
            Instance.setSelectedSisterOrgDataToView(orgList)
        }

        fun setSelectedUserDataToView(userList: List<ExternalUser>) {
            Instance.setSelectedUserDataToView(userList)
        }

        fun setUserDataToView(docExternalUser: DocExternalUser) {
            Instance.setAllUserDataToView(docExternalUser)
        }

        fun deleteOrgElement(org: Organization){
            Instance.deleteOrgElement(org)
        }

        fun deleteSisterOrgElement(org: Organization){
            Instance.deleteSisterOrgElement(org)
        }

        fun deleteUserElement(user: ExternalUser){
            Instance.deleteUserElement(user)
        }

        fun isDialogRunning(): Boolean {
            return Instance.isDialogRunning
        }

        fun dismissDialog() {
            Instance.dismissDialog()
        }
    }
}