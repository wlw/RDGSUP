package com.bricksimple.rdg.pushxbrl;

public class CalCheckList {
    public boolean bIsParent = false;
    public boolean bIsChild = false;
    public int     ParentNdx = 0;
    public int     ChildNdx = 0;
    public boolean bDone = false;
    public boolean bLastChild = false;
    public boolean bPrevParent = false;
    public int     iLastParent = -1;
}
