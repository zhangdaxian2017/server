/*
 * This file is part of the Wildfire Chat package.
 * (c) Heavyrain2012 <heavyrain.lee@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.moquette.imhandler;

import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.proto.WFCMessage;
import com.xiaoleilu.loServer.pojos.GroupNotificationBinaryContent;
import io.moquette.spi.impl.Qos1PublishHandler;
import io.netty.buffer.ByteBuf;
import win.liyufan.im.ErrorCode;
import static win.liyufan.im.IMTopic.KickoffGroupMemberTopic;

@Handler(value = KickoffGroupMemberTopic)
public class KickoffGroupMember extends GroupHandler<WFCMessage.RemoveGroupMemberRequest> {
    @Override
    public ErrorCode action(ByteBuf ackPayload, String clientID, String fromUser, WFCMessage.RemoveGroupMemberRequest request, Qos1PublishHandler.IMCallback callback) {
        ErrorCode errorCode;
        WFCMessage.GroupInfo groupInfo = m_messagesStore.getGroupInfo(request.getGroupId());
        if (groupInfo == null) {
            errorCode = ErrorCode.ERROR_CODE_NOT_EXIST;

        } else if ((groupInfo.getType() == ProtoConstants.GroupType.GroupType_Normal || groupInfo.getType() == ProtoConstants.GroupType.GroupType_Restricted)
            && groupInfo.getOwner() != null && groupInfo.getOwner().equals(fromUser)) {

            //send notify message first, then kickoff the member
            if (request.hasNotifyContent() && request.getNotifyContent().getType() > 0) {
                sendGroupNotification(fromUser, groupInfo.getTargetId(), request.getToLineList(), request.getNotifyContent());
            } else {
                WFCMessage.MessageContent content = new GroupNotificationBinaryContent(fromUser, null, request.getRemovedMemberList()).getKickokfMemberGroupNotifyContent();
                sendGroupNotification(fromUser, request.getGroupId(), request.getToLineList(), content);
            }
            errorCode = m_messagesStore.kickoffGroupMembers(fromUser, request.getGroupId(), request.getRemovedMemberList());
        } else {
            errorCode = ErrorCode.ERROR_CODE_NOT_RIGHT;
        }
        return errorCode;
    }
}
