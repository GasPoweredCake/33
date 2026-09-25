/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.gui.themes.client33;

import com.gaspoweredcake.client33.gui.DefaultSettingsWidgetFactory;
import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.WidgetScreen;
import com.gaspoweredcake.client33.gui.renderer.GuiRenderer;
import com.gaspoweredcake.client33.gui.renderer.packer.GuiTexture;
import com.gaspoweredcake.client33.gui.themes.client33.widgets.*;
import com.gaspoweredcake.client33.gui.themes.client33.widgets.input.WClient33Dropdown;
import com.gaspoweredcake.client33.gui.themes.client33.widgets.input.WClient33Slider;
import com.gaspoweredcake.client33.gui.themes.client33.widgets.input.WClient33TextBox;
import com.gaspoweredcake.client33.gui.themes.client33.widgets.pressable.*;
import com.gaspoweredcake.client33.gui.utils.AlignmentX;
import com.gaspoweredcake.client33.gui.utils.CharFilter;
import com.gaspoweredcake.client33.gui.widgets.*;
import com.gaspoweredcake.client33.gui.widgets.containers.WSection;
import com.gaspoweredcake.client33.gui.widgets.containers.WView;
import com.gaspoweredcake.client33.gui.widgets.containers.WWindow;
import com.gaspoweredcake.client33.gui.widgets.input.WDropdown;
import com.gaspoweredcake.client33.gui.widgets.input.WSlider;
import com.gaspoweredcake.client33.gui.widgets.input.WTextBox;
import com.gaspoweredcake.client33.gui.widgets.pressable.*;
import com.gaspoweredcake.client33.renderer.text.TextRenderer;
import com.gaspoweredcake.client33.settings.*;
import com.gaspoweredcake.client33.systems.accounts.Account;
import com.gaspoweredcake.client33.systems.modules.Module;
import com.gaspoweredcake.client33.utils.render.color.Color;
import com.gaspoweredcake.client33.utils.render.color.SettingColor;
import net.minecraft.client.util.MacWindowUtil;

import static com.gaspoweredcake.client33.Client33.mc;

public class Client33GuiTheme extends GuiTheme {
    private static final Color SCREEN_TINT = new Color(3, 10, 11, 112);
    private static final Color GRID_COLOR = new Color(78, 171, 115, 18);
    private static final String SCREEN_MARK = "33 // СИСТЕМА 2027";

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgTextColors = settings.createGroup("Text");
    private final SettingGroup sgBackgroundColors = settings.createGroup("Background");
    private final SettingGroup sgOutline = settings.createGroup("Outline");
    private final SettingGroup sgSeparator = settings.createGroup("Separator");
    private final SettingGroup sgScrollbar = settings.createGroup("Scrollbar");
    private final SettingGroup sgSlider = settings.createGroup("Slider");
    private final SettingGroup sgStarscript = settings.createGroup("Starscript");

    // General

    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale of the GUI.")
        .defaultValue(1)
        .min(0.75)
        .sliderRange(0.75, 4)
        .onSliderRelease()
        .onChanged(aDouble -> {
            if (mc.currentScreen instanceof WidgetScreen) ((WidgetScreen) mc.currentScreen).invalidate();
        })
        .build()
    );

    public final Setting<AlignmentX> moduleAlignment = sgGeneral.add(new EnumSetting.Builder<AlignmentX>()
        .name("module-alignment")
        .description("How module titles are aligned.")
        .defaultValue(AlignmentX.Center)
        .build()
    );

    public final Setting<Boolean> categoryIcons = sgGeneral.add(new BoolSetting.Builder()
        .name("category-icons")
        .description("Adds item icons to module categories.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> hideHUD = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-HUD")
        .description("Hide HUD when in GUI.")
        .defaultValue(false)
        .onChanged(v -> {
            if (mc.currentScreen instanceof WidgetScreen) mc.options.hudHidden = v;
        })
        .build()
    );

    // Colors

    public final Setting<SettingColor> accentColor = color("accent", "Main color of the GUI.", new SettingColor(105, 242, 150));
    public final Setting<SettingColor> checkboxColor = color("checkbox", "Color of checkbox.", new SettingColor(105, 242, 150));
    public final Setting<SettingColor> plusColor = color("plus", "Color of plus button.", new SettingColor(88, 223, 211));
    public final Setting<SettingColor> minusColor = color("minus", "Color of minus button.", new SettingColor(255, 91, 105));
    public final Setting<SettingColor> favoriteColor = color("favorite", "Color of checked favorite button.", new SettingColor(224, 216, 105));

    // Text

    public final Setting<SettingColor> textColor = color(sgTextColors, "text", "Color of text.", new SettingColor(224, 246, 232));
    public final Setting<SettingColor> textSecondaryColor = color(sgTextColors, "text-secondary-text", "Color of secondary text.", new SettingColor(154, 187, 168));
    public final Setting<SettingColor> textHighlightColor = color(sgTextColors, "text-highlight", "Color of text highlighting.", new SettingColor(105, 242, 150, 110));
    public final Setting<SettingColor> titleTextColor = color(sgTextColors, "title-text", "Color of title text.", new SettingColor(202, 255, 218));
    public final Setting<SettingColor> loggedInColor = color(sgTextColors, "logged-in-text", "Color of logged in account name.", new SettingColor(105, 242, 150));
    public final Setting<SettingColor> placeholderColor = color(sgTextColors, "placeholder", "Color of placeholder text.", new SettingColor(154, 187, 168, 145));

    // Background

    public final ThreeStateColorSetting backgroundColor = new ThreeStateColorSetting(
            sgBackgroundColors,
            "background",
            new SettingColor(9, 19, 21, 244),
            new SettingColor(17, 36, 32, 248),
            new SettingColor(25, 53, 42, 250)
    );

    public final Setting<SettingColor> moduleBackground = color(sgBackgroundColors, "module-background", "Color of module background when active.", new SettingColor(25, 67, 49, 228));

    // Outline

    public final ThreeStateColorSetting outlineColor = new ThreeStateColorSetting(
            sgOutline,
            "outline",
            new SettingColor(46, 92, 72),
            new SettingColor(83, 164, 113),
            new SettingColor(105, 242, 150)
    );

    // Separator

    public final Setting<SettingColor> separatorText = color(sgSeparator, "separator-text", "Color of separator text", new SettingColor(181, 239, 198));
    public final Setting<SettingColor> separatorCenter = color(sgSeparator, "separator-center", "Center color of separators.", new SettingColor(77, 159, 105, 170));
    public final Setting<SettingColor> separatorEdges = color(sgSeparator, "separator-edges", "Color of separator edges.", new SettingColor(44, 91, 67, 50));

    // Scrollbar

    public final ThreeStateColorSetting scrollbarColor = new ThreeStateColorSetting(
            sgScrollbar,
            "Scrollbar",
            new SettingColor(44, 91, 67, 200),
            new SettingColor(75, 151, 102, 220),
            new SettingColor(105, 242, 150, 235)
    );

    // Slider

    public final ThreeStateColorSetting sliderHandle = new ThreeStateColorSetting(
            sgSlider,
            "slider-handle",
            new SettingColor(105, 242, 150),
            new SettingColor(153, 255, 183),
            new SettingColor(202, 255, 218)
    );

    public final Setting<SettingColor> sliderLeft = color(sgSlider, "slider-left", "Color of slider left part.", new SettingColor(75, 186, 117));
    public final Setting<SettingColor> sliderRight = color(sgSlider, "slider-right", "Color of slider right part.", new SettingColor(31, 61, 48));

    // Starscript

    private final Setting<SettingColor> starscriptText = color(sgStarscript, "starscript-text", "Color of text in Starscript code.", new SettingColor(196, 223, 206));
    private final Setting<SettingColor> starscriptBraces = color(sgStarscript, "starscript-braces", "Color of braces in Starscript code.", new SettingColor(117, 215, 151));
    private final Setting<SettingColor> starscriptParenthesis = color(sgStarscript, "starscript-parenthesis", "Color of parenthesis in Starscript code.", new SettingColor(156, 198, 172));
    private final Setting<SettingColor> starscriptDots = color(sgStarscript, "starscript-dots", "Color of dots in starscript code.", new SettingColor(153, 184, 163));
    private final Setting<SettingColor> starscriptCommas = color(sgStarscript, "starscript-commas", "Color of commas in starscript code.", new SettingColor(153, 184, 163));
    private final Setting<SettingColor> starscriptOperators = color(sgStarscript, "starscript-operators", "Color of operators in Starscript code.", new SettingColor(103, 226, 202));
    private final Setting<SettingColor> starscriptStrings = color(sgStarscript, "starscript-strings", "Color of strings in Starscript code.", new SettingColor(222, 207, 131));
    private final Setting<SettingColor> starscriptNumbers = color(sgStarscript, "starscript-numbers", "Color of numbers in Starscript code.", new SettingColor(134, 195, 224));
    private final Setting<SettingColor> starscriptKeywords = color(sgStarscript, "starscript-keywords", "Color of keywords in Starscript code.", new SettingColor(255, 128, 133));
    private final Setting<SettingColor> starscriptAccessedObjects = color(sgStarscript, "starscript-accessed-objects", "Color of accessed objects (before a dot) in Starscript code.", new SettingColor(167, 245, 190));

    public Client33GuiTheme() {
        super("33");

        settingsFactory = new DefaultSettingsWidgetFactory(this);
    }

    @Override
    public void renderBackground(GuiRenderer renderer, double width, double height) {
        renderer.quad(0, 0, width, height, SCREEN_TINT);

        double step = scale(64);
        double line = scale(1);
        for (double x = 0; x < width; x += step) renderer.quad(x, 0, line, height, GRID_COLOR);
        for (double y = 0; y < height; y += step) renderer.quad(0, y, width, line, GRID_COLOR);

        double mark = scale(14);
        double inset = scale(12);
        Color accent = accentColor.get();
        renderer.quad(inset, inset, mark, line, accent);
        renderer.quad(inset, inset, line, mark, accent);
        renderer.quad(width - inset - mark, height - inset - line, mark, line, accent);
        renderer.quad(width - inset - line, height - inset - mark, line, mark, accent);

        renderer.text(SCREEN_MARK, width - textWidth(SCREEN_MARK) - scale(36), height - textHeight() - scale(9), textSecondaryColor.get(), false);
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
        return group.add(new ColorSetting.Builder()
                .name(name + "-color")
                .description(description)
                .defaultValue(color)
                .build());
    }
    private Setting<SettingColor> color(String name, String description, SettingColor color) {
        return color(sgColors, name, description, color);
    }

    // Widgets

    @Override
    public WWindow window(WWidget icon, String title) {
        return w(new WClient33Window(icon, title));
    }

    @Override
    public WLabel label(String text, boolean title, double maxWidth) {
        if (maxWidth == 0 && !text.contains("\n")) return w(new WClient33Label(text, title));
        return w(new WClient33MultiLabel(text, title, maxWidth));
    }

    @Override
    public WHorizontalSeparator horizontalSeparator(String text) {
        return w(new WClient33HorizontalSeparator(text));
    }

    @Override
    public WVerticalSeparator verticalSeparator() {
        return w(new WClient33VerticalSeparator());
    }

    @Override
    protected WButton button(String text, GuiTexture texture) {
        return w(new WClient33Button(text, texture));
    }

    @Override
    protected WConfirmedButton confirmedButton(String text, String confirmText, GuiTexture texture) {
        return w(new WClient33ConfirmedButton(text, confirmText, texture));
    }

    @Override
    public WMinus minus() {
        return w(new WClient33Minus());
    }

    @Override
    public WConfirmedMinus confirmedMinus() {
        return w(new WClient33ConfirmedMinus());
    }

    @Override
    public WPlus plus() {
        return w(new WClient33Plus());
    }

    @Override
    public WCheckbox checkbox(boolean checked) {
        return w(new WClient33Checkbox(checked));
    }

    @Override
    public WSlider slider(double value, double min, double max) {
        return w(new WClient33Slider(value, min, max));
    }

    @Override
    public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return w(new WClient33TextBox(text, placeholder, filter, renderer));
    }

    @Override
    public <T> WDropdown<T> dropdown(T[] values, T value) {
        return w(new WClient33Dropdown<>(values, value));
    }

    @Override
    public WTriangle triangle() {
        return w(new WClient33Triangle());
    }

    @Override
    public WTooltip tooltip(String text) {
        return w(new WClient33Tooltip(text));
    }

    @Override
    public WView view() {
        return w(new WClient33View());
    }

    @Override
    public WSection section(String title, boolean expanded, WWidget headerWidget) {
        return w(new WClient33Section(title, expanded, headerWidget));
    }

    @Override
    public WAccount account(WidgetScreen screen, Account<?> account) {
        return w(new WClient33Account(screen, account));
    }

    @Override
    public WWidget module(Module module, String title) {
        return w(new WClient33Module(module, title));
    }

    @Override
    public WQuad quad(Color color) {
        return w(new WClient33Quad(color));
    }

    @Override
    public WTopBar topBar() {
        return w(new WClient33TopBar());
    }

    @Override
    public WFavorite favorite(boolean checked) {
        return w(new WClient33Favorite(checked));
    }

    // Colors

    @Override
    public Color textColor() {
        return textColor.get();
    }

    @Override
    public Color textSecondaryColor() {
        return textSecondaryColor.get();
    }

    //     Starscript

    @Override
    public Color starscriptTextColor() {
        return starscriptText.get();
    }

    @Override
    public Color starscriptBraceColor() {
        return starscriptBraces.get();
    }

    @Override
    public Color starscriptParenthesisColor() {
        return starscriptParenthesis.get();
    }

    @Override
    public Color starscriptDotColor() {
        return starscriptDots.get();
    }

    @Override
    public Color starscriptCommaColor() {
        return starscriptCommas.get();
    }

    @Override
    public Color starscriptOperatorColor() {
        return starscriptOperators.get();
    }

    @Override
    public Color starscriptStringColor() {
        return starscriptStrings.get();
    }

    @Override
    public Color starscriptNumberColor() {
        return starscriptNumbers.get();
    }

    @Override
    public Color starscriptKeywordColor() {
        return starscriptKeywords.get();
    }

    @Override
    public Color starscriptAccessedObjectColor() {
        return starscriptAccessedObjects.get();
    }

    // Other

    @Override
    public TextRenderer textRenderer() {
        return TextRenderer.get();
    }

    @Override
    public double scale(double value) {
        double scaled = value * scale.get();

        if (MacWindowUtil.IS_MAC) {
            scaled /= (double) mc.getWindow().getWidth() / mc.getWindow().getFramebufferWidth();
        }

        return scaled;
    }

    @Override
    public boolean categoryIcons() {
        return categoryIcons.get();
    }

    @Override
    public boolean hideHUD() {
        return hideHUD.get();
    }

    public class ThreeStateColorSetting {
        private final Setting<SettingColor> normal, hovered, pressed;

        public ThreeStateColorSetting(SettingGroup group, String name, SettingColor c1, SettingColor c2, SettingColor c3) {
            normal = color(group, name, "Color of " + name + ".", c1);
            hovered = color(group, "hovered-" + name, "Color of " + name + " when hovered.", c2);
            pressed = color(group, "pressed-" + name, "Color of " + name + " when pressed.", c3);
        }

        public SettingColor get() {
            return normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor) {
            if (pressed) return this.pressed.get();
            return (hovered && (bypassDisableHoverColor || !disableHoverColor)) ? this.hovered.get() : this.normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered) {
            return get(pressed, hovered, false);
        }
    }
}
