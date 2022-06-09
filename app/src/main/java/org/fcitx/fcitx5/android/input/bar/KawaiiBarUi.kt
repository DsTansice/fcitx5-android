package org.fcitx.fcitx5.android.input.bar

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.text.TextUtils
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.ViewAnimator
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.bar.IdleUiStateMachine.State.*
import org.fcitx.fcitx5.android.input.editing.TextEditingUi.GImageButton
import org.fcitx.fcitx5.android.input.keyboard.CustomGestureView
import org.fcitx.fcitx5.android.utils.borderlessRippleDrawable
import org.fcitx.fcitx5.android.utils.rippleDrawable
import splitties.dimensions.dp
import splitties.views.dsl.constraintlayout.*
import splitties.views.dsl.core.*
import splitties.views.gravityCenter
import splitties.views.gravityVerticalCenter
import splitties.views.imageResource
import splitties.views.padding
import timber.log.Timber

sealed class KawaiiBarUi(override val ctx: Context, protected val inputTheme: Theme) : Ui {

    class Candidate(ctx: Context, inputTheme: Theme, private val horizontalView: View) :
        KawaiiBarUi(ctx, inputTheme) {

        val expandButton = GImageButton(ctx).apply {
            id = R.id.expand_candidate_btn
            imageResource = R.drawable.ic_baseline_expand_more_24
            visibility = ConstraintLayout.INVISIBLE
            colorFilter =
                PorterDuffColorFilter(inputTheme.altKeyTextColor.color, PorterDuff.Mode.SRC_IN)
        }

        override val root = ctx.constraintLayout {
            add(expandButton, lParams(matchConstraints, dp(40)) {
                matchConstraintPercentWidth = 0.1f
                topOfParent()
                endOfParent()
                bottomOfParent()
            })
            add(horizontalView, lParams(matchConstraints, dp(40)) {
                topOfParent()
                startOfParent()
                before(expandButton)
                bottomOfParent()
            })
        }
    }

    class Idle(
        ctx: Context,
        inputTheme: Theme,
        private val getCurrentState: () -> IdleUiStateMachine.State,
    ) : KawaiiBarUi(ctx, inputTheme) {

        private val IdleUiStateMachine.State.menuButtonRotation
            get() =
                if (inPrivate) 0f
                else when (this) {
                    Empty -> -90f
                    Clipboard -> -90f
                    Toolbar -> 90f
                    ToolbarWithClip -> 90f
                    ClipboardTimedOut -> -90f
                }

        private var inPrivate = false

        private fun toolButton(@DrawableRes icon: Int) = GImageButton(ctx).apply {
            imageResource = icon
            background = borderlessRippleDrawable(inputTheme.keyPressHighlightColor.color, dp(20))
            colorFilter =
                PorterDuffColorFilter(inputTheme.altKeyTextColor.color, PorterDuff.Mode.SRC_IN)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            padding = dp(10)
        }

        val menuButton = toolButton(R.drawable.ic_baseline_expand_more_24).apply {
            rotation = getCurrentState().menuButtonRotation
        }

        val undoButton = toolButton(R.drawable.ic_baseline_undo_24)

        val redoButton = toolButton(R.drawable.ic_baseline_redo_24)

        val cursorMoveButton = toolButton(R.drawable.ic_cursor_move)

        val clipboardButton = toolButton(R.drawable.ic_clipboard)

        val moreButton = toolButton(R.drawable.ic_baseline_more_horiz_24)

        val hideKeyboardButton = toolButton(R.drawable.ic_baseline_arrow_drop_down_24)

        private fun ConstraintLayout.addButton(
            v: View,
            initParams: ConstraintLayout.LayoutParams.() -> Unit = {}
        ) {
            add(v, ConstraintLayout.LayoutParams(dp(40), dp(40)).apply {
                topOfParent()
                bottomOfParent()
                initParams(this)
            })
        }

        private val buttonsBar = constraintLayout {
            addButton(undoButton) { startOfParent(); before(redoButton) }
            addButton(redoButton) { after(undoButton); before(cursorMoveButton) }
            addButton(cursorMoveButton) { after(redoButton); before(clipboardButton) }
            addButton(clipboardButton) { after(cursorMoveButton); before(moreButton) }
            addButton(moreButton) { after(clipboardButton); endOfParent() }
        }

        private val clipboardIcon = imageView {
            imageResource = R.drawable.ic_clipboard
            colorFilter =
                PorterDuffColorFilter(inputTheme.altKeyTextColor.color, PorterDuff.Mode.SRC_IN)
        }

        private val clipboardText = textView {
            isSingleLine = true
            maxWidth = dp(120)
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(inputTheme.altKeyTextColor.color)
        }

        private val clipboardSuggestionLayout = horizontalLayout {
            gravity = gravityCenter
            padding = dp(4)
            add(clipboardIcon, lParams(dp(20), dp(20)))
            add(clipboardText, lParams {
                leftMargin = dp(4)
            })
        }

        val clipboardSuggestionItem = object : CustomGestureView(ctx) {
            init {
                visibility = View.INVISIBLE
                background = rippleDrawable(inputTheme.keyPressHighlightColor.color)
                add(clipboardSuggestionLayout, lParams(wrapContent, matchParent))
            }
        }

        private val clipboardBar = constraintLayout {
            add(clipboardSuggestionItem, lParams(wrapContent, matchConstraints) {
                topOfParent()
                startOfParent()
                endOfParent()
                bottomOfParent()
                verticalMargin = dp(4)
            })
        }

        private val animator = ViewAnimator(ctx).apply {
            add(clipboardBar, lParams(matchParent, matchParent))
            add(buttonsBar, lParams(matchParent, matchParent))
        }

        private val onDisableAnimationChange = ManagedPreference.OnChangeListener<Boolean> {
            animator.apply {
                if (!it) {
                    inAnimation = AnimationSet(true).apply {
                        duration = 200L
                        addAnimation(AlphaAnimation(0f, 1f))
                        addAnimation(ScaleAnimation(0f, 1f, 0f, 1f, 0f, dp(20f)))
                        addAnimation(TranslateAnimation(dp(-100f), 0f, 0f, 0f))
                    }
                    outAnimation = AnimationSet(true).apply {
                        duration = 200L
                        addAnimation(AlphaAnimation(1f, 0f))
                        addAnimation(ScaleAnimation(1f, 0f, 1f, 0f, 0f, dp(20f)))
                        addAnimation(TranslateAnimation(0f, dp(-100f), 0f, 0f))
                    }
                } else {
                    inAnimation = null
                    outAnimation = null
                }
            }
        }

        private val disableAnimation = AppPrefs.getInstance().advanced.disableAnimation

        init {
            disableAnimation.registerOnChangeListener(onDisableAnimationChange)
            // After animator was initialized
            onDisableAnimationChange.onChange(disableAnimation.getValue())
        }

        override val root = constraintLayout {
            addButton(menuButton) { startOfParent() }
            add(animator, lParams(matchConstraints, dp(40)) {
                after(menuButton)
                before(hideKeyboardButton)
            })
            addButton(hideKeyboardButton) { endOfParent() }
        }

        fun privateMode(activate: Boolean = true) {
            inPrivate = activate
            menuButton.apply {
                imageResource = if (inPrivate)
                    R.drawable.ic_view_private
                else
                    R.drawable.ic_baseline_expand_more_24
                rotation = getCurrentState().menuButtonRotation
            }
        }

        private fun transitionToClipboardBar() {
            animator.displayedChild = 0
        }

        private fun transitionToButtonsBar() {
            animator.displayedChild = 1
        }

        fun switchUiByState(state: IdleUiStateMachine.State) {
            Timber.d("Switch idle ui to $state")
            when (state) {
                Clipboard -> {
                    transitionToClipboardBar()
                    enableClipboardItem()
                }
                Toolbar -> {
                    transitionToButtonsBar()
                    disableClipboardItem()
                }
                Empty -> {
                    // empty and clipboard share the same view
                    transitionToClipboardBar()
                    disableClipboardItem()
                    setClipboardItemText("")
                }
                ToolbarWithClip -> {
                    transitionToButtonsBar()
                }
                ClipboardTimedOut -> {
                    transitionToClipboardBar()
                }
            }
            if (disableAnimation.getValue())
                menuButton.rotation = state.menuButtonRotation
            else
                menuButton.animate().setDuration(200L).rotation(state.menuButtonRotation)
        }

        private fun enableClipboardItem() {
            clipboardSuggestionItem.visibility = View.VISIBLE
        }

        private fun disableClipboardItem() {
            clipboardSuggestionItem.visibility = View.INVISIBLE
        }

        fun setClipboardItemText(text: String) {
            clipboardText.text = text
        }
    }

    class Title(ctx: Context, inputTheme: Theme) : KawaiiBarUi(ctx, inputTheme) {

        private val backButton = GImageButton(ctx).apply {
            imageResource = R.drawable.ic_baseline_arrow_back_24
            background = borderlessRippleDrawable(inputTheme.keyPressHighlightColor.color, dp(20))
            colorFilter =
                PorterDuffColorFilter(inputTheme.altKeyTextColor.color, PorterDuff.Mode.SRC_IN)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            padding = dp(10)
        }

        private val titleText = textView {
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            setTextColor(inputTheme.altKeyTextColor.color)
            gravity = gravityVerticalCenter
            textSize = 16f
        }

        private var extension: View? = null

        override val root = constraintLayout {
            add(backButton, lParams(dp(40), dp(40)) {
                topOfParent()
                startOfParent()
                bottomOfParent()
            })
            add(titleText, lParams(wrapContent, dp(40)) {
                topOfParent()
                after(backButton, dp(8))
                bottomOfParent()
            })
        }

        fun setReturnButtonOnClickListener(block: () -> Unit) {
            backButton.setOnClickListener {
                block()
            }
        }

        fun setTitle(title: String) {
            titleText.text = title
        }

        fun addExtension(view: View) {
            if (extension != null) {
                throw IllegalStateException("TitleBar extension is already present")
            }
            extension = view
            root.run {
                add(view, lParams(wrapContent, dp(40)) {
                    topOfParent()
                    endOfParent(dp(5))
                    bottomOfParent()
                })
            }
        }

        fun removeExtension() {
            if (extension == null)
                return
            root.removeView(extension)
            extension = null
        }
    }

}