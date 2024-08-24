package ltd.dreamcraft.xinxinautoreview.listeners;

import com.xinxin.BotApi.BotAction;
import com.xinxin.BotEvent.GroupMessageEvent;
import ltd.dreamcraft.xinxinautoreview.XinxinAutoReview;
import ltd.dreamcraft.xinxinautoreview.utils.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnGroupMessage implements Listener {
    /**
     * 监听群聊消息，如果信息内容等于 "成员统计" 就返回统计图
     * 关键词 可以在配置文件 config.yml 中修改
     *
     * @param event 群聊消息事件
     */
    @EventHandler
    public void GroupMsg(GroupMessageEvent event) {
        if (XinxinAutoReview.getInstance().getConfig().getStringList("Settings.GroupList").contains(String.valueOf(event.getGroup_id()))
                && event.getMessage().equalsIgnoreCase(XinxinAutoReview.instance.getConfig().getString("Settings.GroupKeywordListen"))) {
            String message = MessageUtil.bufferedImgToMsg(XinxinAutoReview.generatePieChart(XinxinAutoReview.matchedCategoriesCount));
            BotAction.sendGroupMessage(event.getGroup_id(), message);
        }
    }
}
