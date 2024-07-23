package com.knowledgeways.idocs.utils

import com.knowledgeways.idocs.network.model.Document
import com.knowledgeways.idocs.network.model.documentdetail.DocumentDetail

object AppConstants {
    const val APP_NAME = "iDOX"
    const val DB_NAME = "idox.db"
    const val APP_PREF_NAME = "iDOX_PREFERENCE"
    const val IMAGE_URL = "/images/"
    const val ICON_URL = "/icons/"
    const val PDF_URL  = "/pdfs/"
    const val FILE_NAME_IMAGE_ZIP = "-images.zip"
    const val FILE_NAME_ICON_ZIP = "-icons.zip"
    const val FILE_TYPE_IMAGE = 0
    const val FILE_TYPE_ICON = 1
    const val FILE_TYPE_PDF = 2
    const val BASE_URL = "http://3.133.130.191:8080/kw-ecm/idox"
    const val BOX_ICON_SUFFIX_SELECTED = "-selected@2x"
    const val BOX_ICON_SUFFIX_DESELECTED = "@2x"
    const val FILE_NAME_PNG = ".png"

    const val TYPE_INTERNAL = "INTERNAL"

    const val DOCUMENT_PAGE_SIZE = 10

    const val BOX_ID_SEARCH = "search"

    const val KEYWORD_DOCUMENT = "document"
    const val KEYWORD_DOCUMENT_DETAIL = "document_detail"
    const val KEYWORD_TOOLBAR_COLOR = "toolbar_color"
    const val KEYWORD_BACKGROUND_COLOR = "background_color"
    const val KEYWORD_ROOT_LINK_ID = "root_link_id"
    const val KEYWORD_FROM_ATTACHMENT = "keyword_from_attachment"
    const val KEYWORD_ATTACHMENT_DOCUMENT_ID = "attachment_document_id"
    const val KEYWORD_ATTACHMENT_ID = "attachment_id"
    const val KEYWORD_FROM_PDF_LIST = "from_pdf_list"
    const val KEYWORD_PDF_SIZE = "pdf_size"
    const val KEYWORD_SELECTED_POSITION = "selected_position"

    const val REQUEST_VALUE_PERMISSION_SETTINGS = 2296
    const val REQUEST_VALUE_URL_SETTINGS = 2297
    var mDocumentList: ArrayList<Document> = ArrayList()

    //Message for Event bus
    const val MESSAGE_SELECTED_ORG_CHANGED = 10000
    const val MESSAGE_SELECTED_SISTER_ORG_CHANGED = 10001
    const val MESSAGE_SELECTED_USER_CHANGED = 10002
    const val MESSAGE_UNARCHIVE_DONE = 10003


    const val MESSAGE_ADD_SUCCESS = "Added successfully"
    const val MESSAGE_DELETE_SUCCESS = "Deleted Successfully"

}