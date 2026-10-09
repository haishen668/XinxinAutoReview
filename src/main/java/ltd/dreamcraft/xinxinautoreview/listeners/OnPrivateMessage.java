package ltd.dreamcraft.xinxinautoreview.listeners;

import com.xinxin.BotApi.BotAction;
import com.xinxin.BotEvent.PrivateMessageEvent;
import ltd.dreamcraft.xinxinautoreview.XinxinAutoReview;
import ltd.dreamcraft.xinxinautoreview.utils.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author haishen668
 * @version 1.0
 * @description: 监听私聊消息
 * @date 2024/4/6 1:01
 */
public class OnPrivateMessage implements Listener {
    /**
     * 监听私聊消息，如果信息内容等于 "成员统计" 就返回统计图
     * 关键词 可以在配置文件 config.yml 中修改
     *
     * @param event 私聊消息事件
     */
    @EventHandler
    public void PrivateMsg(PrivateMessageEvent event){
        if(XinxinAutoReview.getInstance().getConfig().getBoolean("Settings.MessagePush")
        && event.getUser_id() == XinxinAutoReview.getInstance().getConfig().getLong("Settings.admin")
        && event.getMessage().trim().equals(XinxinAutoReview.getInstance().getConfig().getString("Settings.GroupKeywordListen"))){
            String ImgBase64Str = MessageUtil.bufferedImgToMsg(XinxinAutoReview.generatePieChart(XinxinAutoReview.matchedCategoriesCount));
            BotAction.sendPrivateMessage(event.getUser_id(), ImgBase64Str);
        }
    }
}
