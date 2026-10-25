package com.popstars.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友页需要的三份名单。
 */
public class FriendBoard {

    public List<FriendView> friends = new ArrayList<>();
    public List<FriendView> incoming = new ArrayList<>();
    public List<FriendView> outgoing = new ArrayList<>();
}
