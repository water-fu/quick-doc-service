package cn.mxleader.quickdoc.common;

import cn.mxleader.quickdoc.entities.FsOwner;

public class CommonCode {

    public static final String HOME_TITLE = "快捷文档共享";

    public static final String SESSION_USER = "ActiveUser";

    public static final Integer READ_PRIVILEGE = 1;
    public static final Integer WRITE_PRIVILEGE = 2;
    public static final Integer DELETE_PRIVILEGE = 4;

    public static final FsOwner SYSTEM_PUBLIC_OWNER = new FsOwner("public",
            FsOwner.Type.TYPE_PUBLIC, 1);
    public static final FsOwner SYSTEM_ADMIN_GROUP_OWNER = new FsOwner("administrators",
            FsOwner.Type.TYPE_GROUP, 7);

}
