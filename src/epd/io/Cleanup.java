package epd.io;

import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.epd.EpdMethodExtension;
import org.openlca.ilcd.flows.epd.matml.MaterialDoc;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.processes.Time;
import org.openlca.ilcd.processes.epd.EpdContentDeclaration;
import org.openlca.ilcd.processes.epd.EpdInfoExtension;
import org.openlca.ilcd.processes.epd.EpdInventoryMethodExtension;
import org.openlca.ilcd.processes.epd.EpdPublicationExtension;
import org.openlca.ilcd.processes.epd.EpdRepresentativenessExtension;
import org.openlca.ilcd.processes.epd.EpdSafetyMargins;
import org.openlca.ilcd.processes.epd.EpdScenarioData;
import org.openlca.ilcd.processes.epd.EpdServiceLife;
import org.openlca.ilcd.processes.epd.EpdTimeExtension;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Processes;

import com.google.common.base.Strings;


/**
 * Remove empty elements so that the data set validation is happy.
 */
public class Cleanup {

	public static void on(Flow flow) {
		if (flow == null)
			return;
		var info = Flows.getDataSetInfo(flow);
		if (info != null && info.getEpdExtension() != null) {
			var ext = info.getEpdExtension();
			if (isEmpty(ext.getMaterialDoc())
					&& ext.getGenericFlow() == null
					&& ext.getAny().isEmpty()) {
				info.withEpdExtension(null);
			}
		}
		var method = Flows.getInventoryMethod(flow);
		if (method != null && isEmpty(method.getEpdExtension())) {
			method.withEpdExtension(null);
		}
	}

	private static boolean isEmpty(MaterialDoc doc) {
		if (doc == null)
			return true;
		return doc.getMaterials().isEmpty()
				&& doc.getProperties().isEmpty();
	}

	private static boolean isEmpty(EpdMethodExtension ext) {
		if (ext == null)
			return true;
		return ext.getVendorSpecific() == null
				&& ext.getVendor() == null
				&& ext.getDocumentation() == null
				&& ext.getAny().isEmpty();
	}

	public static void on(Process epd) {
		if (epd == null)
			return;

		var info = Epds.getProcessInfo(epd);
		if (info != null && isEmpty(info.getTime())) {
			info.withTime(null);
		}
		var time = Epds.getTime(epd);
		if (time != null && isEmpty(time.getEpdExtension())) {
			time.withEpdExtension(null);
		}

		if (info != null && isEmpty(info.getGeography())) {
			info.withGeography(null);
		}
		if (info != null && isEmpty(info.getTechnology())) {
			info.withTechnology(null);
		}

		var dsInfo = Epds.getDataSetInfo(epd);
		if (dsInfo != null) {
			var ext = dsInfo.getEpdExtension();
			if (isEmpty(ext)) {
				dsInfo.withEpdExtension(null);
			} else {
				if (isEmpty(ext.getSafetyMargins())){
					ext.withSafetyMargins(null);
				}
				if (isEmpty(ext.getContentDeclaration())) {
					ext.withContentDeclaration(null);
				}
				if (isEmpty(ext.getReferenceServiceLife())) {
					ext.withReferenceServiceLife(null);
				}
				if (isEmpty(ext.getEstimatedServiceLife())) {
					ext.withEstimatedServiceLife(null);
				}
				if (isEmpty(ext.getScenarioData())) {
					ext.withScenarioData(null);
				}
			}
		}

		var adminInfo = Processes.getAdminInfo(epd);

		// bug #59, remove empty commissioner and goal types
		if (adminInfo != null && adminInfo.getCommissionerAndGoal() != null) {
			var comGoal = adminInfo.getCommissionerAndGoal();
			if (isEmpty(comGoal.getOther())) {
				comGoal.withOther(null);
			}
			if (isEmpty(comGoal)) {
				adminInfo.withCommissionerAndGoal(null);
			}
		}

		if (adminInfo != null && isEmpty(adminInfo.getDataGenerator())) {
			adminInfo.withDataGenerator(null);
		}

		var rep = Epds.getRepresentativeness(epd);
		if (rep != null && isEmpty(rep.getEpdExtension())) {
			rep.withEpdExtension(null);
		}

		var method = Epds.getInventoryMethod(epd);
		if (method != null && isEmpty(method.getEpdExtension())) {
			method.withEpdExtension(null);
		}

		var pub = Epds.getPublication(epd);
		if (pub != null && isEmpty(pub.getEpdExtension())) {
			pub.withEpdExtension(null);
		}
	}

	private static boolean isEmpty(CommissionerAndGoal comGoal) {
		if (comGoal == null)
			return true;
		return comGoal.getCommissioners().isEmpty()
				&& comGoal.getIntendedApplications().isEmpty()
				&& comGoal.getProject().isEmpty()
				&& isEmpty(comGoal.getOther())
				&& comGoal.getOtherAttributes().isEmpty();
	}

	private static boolean isEmpty(Time time) {
		if (time == null)
			return true;
		return time.getDescription().isEmpty()
				&& time.getReferenceYear() == null
				&& time.getValidUntil() == null
				&& isEmpty(time.getEpdExtension())
				&& time.getOtherAttributes().isEmpty();
	}

	private static boolean isEmpty(Geography geography) {
		if (geography == null)
			return true;
		return isEmpty(geography.getLocation())
				&& geography.getSubLocations().isEmpty()
				&& isEmpty(geography.getOther())
				&& geography.getOtherAttributes().isEmpty();
	}

	private static boolean isEmpty(Technology technology) {
		if (technology == null)
			return true;
		return technology.getApplicability().isEmpty()
				&& technology.getDescription().isEmpty()
				&& technology.getIncludedProcesses().isEmpty()
				&& technology.getPictogram() == null
				&& technology.getPictures().isEmpty()
				&& isEmpty(technology.getOther())
				&& technology.getOtherAttributes().isEmpty();
	}

	private static boolean isEmpty(Location location) {
		if (location == null)
			return true;
		return Strings.isNullOrEmpty(location.getCode())
				&& location.getDescription().isEmpty()
				&& Strings.isNullOrEmpty(location.getLatitudeAndLongitude())
				&& isEmpty(location.getOther())
				&& location.getOtherAttributes().isEmpty();
	}

	private static boolean isEmpty(DataGenerator generator) {
		if (generator == null)
			return true;
		return generator.getContacts().isEmpty()
				&& isEmpty(generator.getOther())
				&& generator.getOtherAttributes().isEmpty();
	}

	private static boolean isEmpty(EpdTimeExtension ext) {
		if (ext == null)
			return true;
		return ext.getPublicationDate() == null
				&& ext.getExpirationDate() == null
				&& ext.getAny().isEmpty();
	}

	private static boolean isEmpty(Other other) {
		return other == null || other.getAny().isEmpty();
	}

	static boolean isEmpty(EpdInfoExtension ext) {
		if (ext == null)
			return true;
		return isEmpty(ext.getContentDeclaration())
				&& ext.getModuleEntries().isEmpty()
				&& ext.getScenarios().isEmpty()
				&& isEmpty(ext.getSafetyMargins())
				&& ext.getProductIds().isEmpty()
				&& isEmpty(ext.getReferenceServiceLife())
				&& isEmpty(ext.getEstimatedServiceLife())
				&& isEmpty(ext.getScenarioData())
				&& ext.getSvhc() == null
				&& ext.getAny().isEmpty();
	}

	private static boolean isEmpty(EpdContentDeclaration dec) {
		return dec == null || dec.getElements().isEmpty();
	}

	private static boolean isEmpty(EpdSafetyMargins esm) {
		if (esm == null)
			return true;
		return esm.getValue() == null && esm.getDescription().isEmpty();
	}

	private static boolean isEmpty(EpdServiceLife serviceLife) {
		if (serviceLife == null)
			return true;
		return serviceLife.getYears() == 0
				&& serviceLife.getConditionFactors().isEmpty()
				&& serviceLife.getStandards().isEmpty()
				&& serviceLife.getDocumentations().isEmpty()
				&& serviceLife.getComments().isEmpty();
	}

	private static boolean isEmpty(EpdScenarioData data) {
		if (data == null)
			return true;
		return data.getUseStageData().isEmpty()
				&& data.getEolData().isEmpty();
	}

	private static boolean isEmpty(EpdRepresentativenessExtension ext) {
		if (ext == null)
			return true;
		return ext.getOriginalEpds().isEmpty()
				&& ext.getManufacturers().isEmpty()
				&& ext.getAny().isEmpty();
	}

	private static boolean isEmpty(EpdInventoryMethodExtension ext) {
		if (ext == null)
			return true;
		return ext.getSubType() == null
				&& ext.getPcrCompliance() == null
				&& ext.getVariability() == null
				&& ext.getAny().isEmpty();
	}

	private static boolean isEmpty(EpdPublicationExtension ext) {
		if (ext == null)
			return true;
		return ext.getPublishers().isEmpty()
				&& ext.getAny().isEmpty();
	}

}
