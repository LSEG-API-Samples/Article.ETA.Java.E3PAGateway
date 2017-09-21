package com.thomsonreuters.atr.gateway.fix.log;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class TestFixField {

    @Test
    public void testFromValues(){
        String aa = "2=advid,3=advrefid,4=side,5=transtype,6=avgpx,11=clordid,14=cumqty,20=exectranstype,22=securityidsource,23=ioiid,26=ioirefid,27=size,28=transtype,31=lastpx,32=lastshares,34=msgseq,35=msgtype,37=orderid,38=ordqty,39=orderstatus,41=origclordid,44=price,48=securityid,53=size,54=side,55=symbol,58=text,115=onbehalfofcompid,128=delivertocompid,150=exectype,151=leavesqty";
        List<FixField> fixFields = FixField.fromKeyValues(aa);
        Assert.assertNotNull(fixFields);
        Assert.assertEquals(fixFields.get(0).getTag(), 2);
        Assert.assertEquals(fixFields.get(0).getAtrName(), "advid");
        Assert.assertEquals(fixFields.get(1).getTag(), 3);
        Assert.assertEquals(fixFields.get(1).getAtrName(), "advrefid");
    }
}
