package com.knowledgeways.idocs.ui.component


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout

open class RtlSwipeLayout : SwipeLayout {
    private var isExchanged = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        checkLayoutDirection()
    }

    override fun setLeftSwipeEnabled(leftSwipeEnabled: Boolean): Unit {
        if (isExchanged) {
            super.setRightSwipeEnabled(leftSwipeEnabled)
        } else {
            super.setLeftSwipeEnabled(leftSwipeEnabled)
        }
    }

    override fun setRightSwipeEnabled(rightSwipeEnabled: Boolean): Unit {
        if (isExchanged) {
            super.setLeftSwipeEnabled(rightSwipeEnabled)
        } else {
            super.setRightSwipeEnabled(rightSwipeEnabled)
        }
    }

    override fun addDrag(dragEdge: DragEdge?, child: View?) {
        addDrag(dragEdge, child, null)
    }

    override fun addDrag(dragEdge: DragEdge?, child: View?, params: ViewGroup.LayoutParams?) {
        val exchangedDragEdge = when (dragEdge) {
            DragEdge.Left -> if (isExchanged) DragEdge.Right else DragEdge.Left
            DragEdge.Right -> if (isExchanged) DragEdge.Left else DragEdge.Right
            else -> dragEdge
        }

        super.addDrag(exchangedDragEdge, child, params)
    }


    private fun checkLayoutDirection() {
        if (!isExchanged && layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            exchangeDirection()
            isExchanged = true
        } else if (isExchanged && layoutDirection == View.LAYOUT_DIRECTION_LTR) {
            exchangeDirection()
            isExchanged = false
        }
    }

    private fun exchangeDirection() {
        // exchange left and right flags
        val orgLeftSwipeEnabled = isLeftSwipeEnabled
        super.setLeftSwipeEnabled(isRightSwipeEnabled)
        super.setRightSwipeEnabled(orgLeftSwipeEnabled)

        // exchange dragEdge views
        val orgLeftDragEdges = dragEdgeMap[DragEdge.Left]
        dragEdgeMap[DragEdge.Left] = dragEdgeMap[DragEdge.Right]
        dragEdgeMap[DragEdge.Right] = orgLeftDragEdges
    }

}