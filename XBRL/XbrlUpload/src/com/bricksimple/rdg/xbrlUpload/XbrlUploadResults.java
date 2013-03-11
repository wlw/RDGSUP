package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;

public  class XbrlUploadResults

{
    public int error = 0;
    public ArrayList<SubmissionList> successfulSubmissions = new ArrayList<SubmissionList>( );
    public ArrayList<SubmissionList> failedSubmissions = new ArrayList<SubmissionList>( );

};
