package me.tepis.integratednbt;

//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.FontRenderer;
//import net.minecraft.client.gui.fonts.Font;
//import net.minecraft.client.renderer.texture.TextureManager;
//import net.minecraft.util.text.TextFormatting;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.lang.Character.UnicodeBlock;
//import java.lang.ref.WeakReference;
//import java.util.Objects;

/**
 * A font render that can render CJK characters without messing up line wrapping.
 */
public class SensibleFontRenderer {//} extends FontRenderer {
//    private static final Logger LOGGER = LogManager.getLogger(
//        IntegratedNBT.MODID + " Sensible Font Renderer");
//    private static WeakReference<FontRenderer> lastSourceFontRendererRef;
//    private static SensibleFontRenderer lastSensibleFontRenderer;
//
//    private SensibleFontRenderer(TextureManager textureManagerIn, Font fontIn) {
//        super(textureManagerIn, fontIn);
//        LOGGER.info("New sensible font renderer created.");
//    }
//
//    public static SensibleFontRenderer get() {
//        FontRenderer newSourceFontRenderer = Minecraft.getInstance().fontRenderer;
//        if (
//            lastSourceFontRendererRef == null ||
//                !Objects.equals(lastSourceFontRendererRef.get(), newSourceFontRenderer)
//        ) {
//            lastSensibleFontRenderer = new SensibleFontRenderer(
//                newSourceFontRenderer.textureManager,
//                newSourceFontRenderer.font
//            );
//            lastSourceFontRendererRef = new WeakReference<>(newSourceFontRenderer);
//        }
//        return lastSensibleFontRenderer;
//    }
//
//    @Override
//    public int sizeStringToWidth(String str, int wrapWidth) {
//        wrapWidth = Math.max(1, wrapWidth);
//        int strSize = str.length();
//        float currentWidth = 0.0F;
//        int usingSize = 0;
//        int wrappingIndex = -1;
//        boolean isBold = false;
//        for (boolean flag1 = true; usingSize < strSize; ++usingSize) {
//            char character = str.charAt(usingSize);
//            boolean canWrap = character == ' ' || isCJKChar(character);
//            switch (character) {
//                case '\n':
//                    --usingSize;
//                    break;
//                default:
//                    if (canWrap) {
//                        wrappingIndex = usingSize;
//                    }
//                    if (currentWidth != 0.0F) {
//                        flag1 = false;
//                    }
//                    currentWidth += this.getCharWidth(character);
//                    if (isBold) {
//                        ++currentWidth;
//                    }
//                    break;
//                case '\u00a7':
//                    if (usingSize < strSize - 1) {
//                        ++usingSize;
//                        TextFormatting textformatting =
//                            TextFormatting.fromFormattingCode(str.charAt(
//                                usingSize));
//                        if (textformatting == TextFormatting.BOLD) {
//                            isBold = true;
//                        } else if (textformatting != null && textformatting.isNormalStyle()) {
//                            isBold = false;
//                        }
//                    }
//            }
//            if (character == '\n') {
//                ++usingSize;
//                wrappingIndex = usingSize;
//                break;
//            }
//            if (currentWidth > (float) wrapWidth) {
//                if (flag1) {
//                    ++usingSize;
//                }
//                break;
//            }
//        }
//        return usingSize != strSize && wrappingIndex != -1 && wrappingIndex < usingSize
//            ? wrappingIndex
//            : usingSize;
//    }
//
//    // https://stackoverflow.com/questions/1499804/how-can-i-detect-japanese-text-in-a-java-string
//    private static boolean isCJKChar(char c) {
//        UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(c);
//        return (unicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
//            || (unicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A)
//            || (unicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B)
//            || (unicodeBlock == UnicodeBlock.CJK_COMPATIBILITY_FORMS)
//            || (unicodeBlock == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS)
//            || (unicodeBlock == UnicodeBlock.CJK_RADICALS_SUPPLEMENT)
//            || (unicodeBlock == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)
//            || (unicodeBlock == UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS);
//    }
}
