package org.fcitx.fcitx5.android.input.keyboard

import android.graphics.Typeface
import androidx.annotation.DrawableRes
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.KeySym

class SymbolKey(
    val symbol: String,
    percentWidth: Float = 0.1f
) : KeyDef(
    Appearance.Text(
        displayText = symbol,
        textSize = 20f,
        typeface = Typeface.NORMAL,
        percentWidth
    ),
    Behavior.Press(
        action = KeyAction.FcitxKeyAction(symbol)
    )
)

class AlphaBetKey(
    val character: String,
    val punctuation: String
) : KeyDef(
    Appearance.AltText(
        displayText = character,
        altText = punctuation,
        textSize = 20f,
        typeface = Typeface.NORMAL
    ),
    Behavior.LongPress(
        action = KeyAction.FcitxKeyAction(character),
        longPressAction = KeyAction.FcitxKeyAction(punctuation)
    )
)

class CapsKey : KeyDef(
    Appearance.Image(
        src = R.drawable.ic_baseline_keyboard_capslock0_24,
        tint = android.R.attr.colorControlNormal,
        viewId = R.id.button_caps,
        percentWidth = 0.15f
    ),
    Behavior.Press(
        action = KeyAction.CapsAction(false)
    )
)

class LayoutSwitchKey(
    displayText: String,
    val to: String = "",
    percentWidth: Float = 0.15f
) : KeyDef(
    Appearance.Text(
        displayText,
        textSize = 16f,
        typeface = Typeface.BOLD,
        percentWidth
    ),
    Behavior.Press(
        action = KeyAction.LayoutSwitchAction(to)
    )
)

class BackspaceKey : KeyDef(
    Appearance.Image(
        src = R.drawable.ic_baseline_backspace_24,
        tint = android.R.attr.colorControlNormal,
        percentWidth = 0.15f,
        viewId = R.id.button_backspace
    ),
    Behavior.Repeat(
        action = KeyAction.SymAction(KeySym(0xff08u))
    )
)

class QuickPhraseKey : KeyDef(
    Appearance.Image(
        src = R.drawable.ic_baseline_format_quote_24,
        tint = android.R.attr.colorControlNormal,
        viewId = R.id.button_quickphrase
    ),
    Behavior.LongPress(
        action = KeyAction.QuickPhraseAction,
        longPressAction = KeyAction.UnicodeAction
    )
)

class LanguageKey : KeyDef(
    Appearance.Image(
        src = R.drawable.ic_baseline_language_24,
        tint = android.R.attr.colorControlNormal,
        viewId = R.id.button_lang
    ),
    Behavior.LongPress(
        action = KeyAction.LangSwitchAction,
        longPressAction = KeyAction.InputMethodSwitchAction
    )
)

class SpaceKeyDef : KeyDef(
    Appearance.Text(
        displayText = " ",
        textSize = 13f,
        typeface = Typeface.NORMAL,
        percentWidth = 0f,
        viewId = R.id.button_space
    ),
    Behavior.Press(
        action = KeyAction.SymAction(KeySym(0x0020u))
    )
)

class ReturnKey : KeyDef(
    Appearance.Image(
        src = R.drawable.ic_baseline_keyboard_return_24,
        tint = android.R.attr.colorForegroundInverse,
        percentWidth = 0.15f,
        background = android.R.attr.colorAccent,
        viewId = R.id.button_return
    ),
    Behavior.Press(
        action = KeyAction.SymAction(KeySym(0xff0du))
    )
)

class ImageLayoutSwitchKey(
    @DrawableRes
    icon: Int,
    to: String,
    percentWidth: Float = 0.1f
) : KeyDef(
    Appearance.Image(
        src = icon,
        tint = android.R.attr.colorControlNormal,
        percentWidth
    ),
    Behavior.Press(
        action = KeyAction.LayoutSwitchAction(to)
    )
)

class MiniSpaceKey : KeyDef(
    Appearance.Image(
        src = R.drawable.ic_baseline_space_bar_24,
        tint = android.R.attr.colorControlNormal,
        percentWidth = 0.15f,
        viewId = R.id.button_mini_space
    ),
    Behavior.Repeat(
        action = KeyAction.SymAction(KeySym(0x0020u))
    )
)
