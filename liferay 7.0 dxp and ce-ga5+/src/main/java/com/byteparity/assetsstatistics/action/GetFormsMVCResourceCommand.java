package com.byteparity.assetsstatistics.action;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;

import com.byteparity.assetsstatistics.common.util.LiferayAssetsStatisticsUtil;
import com.byteparity.assetsstatistics.constants.LiferayAssetsStatisticsPortletKeys;
import com.liferay.dynamic.data.lists.model.DDLRecord;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.service.DDLRecordSetLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

@Component(property = { 
		"javax.portlet.name=" + LiferayAssetsStatisticsPortletKeys.PORTLET_ID,
		"mvc.command.name=/get_forms" }, service = MVCResourceCommand.class)

public class GetFormsMVCResourceCommand implements MVCResourceCommand {

	private static Log _log = LogFactoryUtil.getLog(GetFormsMVCResourceCommand.class.getName());

	@Override
	public boolean serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws PortletException {
		
		//Get Date Format
		HttpSession httpSession = PortalUtil.getHttpServletRequest(resourceRequest).getSession();
		ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		String dateFormat = (String) httpSession.getAttribute("selectedDateFormat");
				
		long groupId = 0;
		long companyId = themeDisplay.getCompanyId();
		try {
			resourceResponse.getWriter().println(getForms(groupId, companyId,dateFormat));
		} catch (IOException e) {
			_log.error(e.getMessage());
		}
		return false;
	}

	/**
	 * Return Forms
	 * 
	 * @param groupId
	 * @param companyId
	 * @return
	 */
	public static JSONObject getForms(long groupId, long companyId,String dateFormat) {
		List<List<Object>> recordsList = new ArrayList<List<Object>>();
		DynamicQuery recordSetQuery = DDLRecordSetLocalServiceUtil.dynamicQuery();
		if(groupId > 0){
			recordSetQuery.add(PropertyFactoryUtil.forName("groupId").eq(groupId));
		}
		if(companyId > 0){
			recordSetQuery.add(PropertyFactoryUtil.forName("companyId").eq(companyId));
		}
		List<DDLRecordSet> ddlRecordSets = DDLRecordSetLocalServiceUtil.dynamicQuery(recordSetQuery);
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);

		for (DDLRecordSet ddlRecordSet : ddlRecordSets) {
			List<Object> row = new ArrayList<Object>();
			try {
				row.add(ddlRecordSet.getRecordSetId());
				row.add(ddlRecordSet.getName(Locale.getDefault()));
				row.add(ddlRecordSet.getUserName());

				// GET DDL RECORD ENTRY
				List<DDLRecord> ddlRecords = ddlRecordSet.getRecords();

				row.add(ddlRecords.size());
				row.add(format.format(ddlRecordSet.getCreateDate()));
				row.add(format.format(ddlRecordSet.getModifiedDate()));
				recordsList.add(row);

			} catch (Exception e) {
				_log.error(e.getMessage());
			}
		}
		return LiferayAssetsStatisticsUtil.getJsonData(ddlRecordSets.size(), null, recordsList, tableFields());
	}

	/**
	 * Return Table fields
	 * 
	 * @return
	 */
	private static JSONArray tableFields() {
		JSONArray field = JSONFactoryUtil.createJSONArray();
		field.put("Record Set Id");
		field.put("Title");
		field.put("Creator");
		field.put("Total Entries");
		field.put("Created Date");
		field.put("Modified Date");
		return field;
	}
}
