/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.edex.registry.acp.xacml.engine.function.impl;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import com.raytheon.uf.edex.registry.acp.xacml.engine.function.CommonFunctions;
import com.raytheon.uf.edex.registry.acp.xacml.engine.function.XACMLFunction;

/**
 * 
 *
 *
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 8/17/2012    724          bphillip    Initial Coding
 * </pre>
 * 
 * @author bphillip
 * @version 1
 */
public class DateTimeSubset extends XACMLFunction {

    private static final String FUNCTION_ID = "urn:oasis:names:tc:xacml:1.0:function:dateTime-subset";

    @Override
    protected String getFunctionId() {
        return FUNCTION_ID;
    }

    public Boolean executeFunction(List<XMLGregorianCalendar> list1,
            List<XMLGregorianCalendar> list2) {
        return CommonFunctions.isSubset(list1, list2);
    }

}
