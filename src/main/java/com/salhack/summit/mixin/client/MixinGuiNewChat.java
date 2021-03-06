package com.salhack.summit.mixin.client;

import com.google.common.collect.Lists;

import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.main.Wrapper;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat
{
    /** Chat lines to be displayed in the chat box */
    @Shadow @Final
    public final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
    /** List of the ChatLines currently drawn */
    @Shadow @Final
    public final List<ChatLine> drawnChatLines = Lists.<ChatLine>newArrayList();
    @Shadow
    public int scrollPos;
    @Shadow
    public boolean isScrolled;
    
    @Inject(method = "setChatLine", at = @At("HEAD"), cancellable = true)
    private void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, CallbackInfo info)
    {
        if (SummitStatic.CHATMODIFICATIONS != null)
        {
            info.cancel();
            
            int maxLines = SummitStatic.CHATMODIFICATIONS.ChatLength.getValue() == -1 ? 0xFFFFFF : SummitStatic.CHATMODIFICATIONS.ChatLength.getValue();
            GuiNewChat guiNewChat = (GuiNewChat) (Object) this;

            if (chatLineId != 0)
            {
                guiNewChat.deleteChatLine(chatLineId);
            }

            int i = MathHelper.floor((float)guiNewChat.getChatWidth() / guiNewChat.getChatScale());
            List<ITextComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, Wrapper.GetMC().fontRenderer, false, false);
            boolean flag = guiNewChat.getChatOpen();

            for (ITextComponent itextcomponent : list)
            {
                if (flag && this.scrollPos > 0)
                {
                    this.isScrolled = true;
                    guiNewChat.scroll(1);
                }

                this.drawnChatLines.add(0, new ChatLine(updateCounter, itextcomponent, chatLineId));
            }

            while (this.drawnChatLines.size() > maxLines)
            {
                this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
            }

            if (!displayOnly)
            {
                this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));

                while (this.chatLines.size() > maxLines)
                {
                    this.chatLines.remove(this.chatLines.size() - 1);
                }
            }
        }
    }
}
