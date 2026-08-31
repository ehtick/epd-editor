package epd.conversion;

import static org.junit.Assert.*;

import javax.xml.datatype.DatatypeFactory;

import org.junit.Test;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdManufacturer;
import org.openlca.ilcd.processes.epd.EpdProductId;
import org.openlca.ilcd.processes.epd.EpdSite;
import org.openlca.ilcd.processes.epd.EpdUseStageData;
import org.openlca.ilcd.util.Epds;

import epd.io.Cleanup;

public class CleanupTest {

	@Test
	public void keepsManufacturersWithSites() {
		var epd = new Process();
		var manu = new EpdManufacturer();
		manu.withSites()
			.add(new EpdSite().withName("Site A"));
		Epds.withManufacturers(epd).add(manu);

		Cleanup.on(epd);

		var rep = Epds.getRepresentativeness(epd);
		assertNotNull(rep);
		var ext = rep.getEpdExtension();
		assertNotNull(ext);
		assertEquals(1, ext.getManufacturers().size());
		assertEquals(1, ext.getManufacturers().getFirst().getSites().size());
		assertTrue(ext.getOriginalEpds().isEmpty());
	}

	@Test
	public void keepsExpirationDate() {
		var epd = new Process();
		var date = DatatypeFactory.newDefaultInstance()
				.newXMLGregorianCalendar();
		date.setYear(2027);
		date.setMonth(3);
		date.setDay(15);
		Epds.withExpirationDate(epd, date);

		Cleanup.on(epd);

		var time = Epds.getTime(epd);
		assertNotNull(time);
		assertNotNull(time.getEpdExtension());
		assertEquals(date, Epds.getExpirationDate(epd));
	}

	@Test
	public void keepsProductIds() {
		var epd = new Process();
		Epds.withProductIds(epd).add(new EpdProductId()
				.withType("GTIN")
				.withValue("1234567"));

		Cleanup.on(epd);

		var ext = Epds.getInfoExtension(epd);
		assertNotNull(ext);
		assertEquals(1, ext.getProductIds().size());
		assertEquals("GTIN", ext.getProductIds().getFirst().getType());
		assertEquals("1234567", ext.getProductIds().getFirst().getValue());
	}

	@Test
	public void keepsReferenceServiceLife() {
		var epd = new Process();
		Epds.withReferenceServiceLife(epd).withYears(50);

		Cleanup.on(epd);

		var ext = Epds.getInfoExtension(epd);
		assertNotNull(ext);
		assertNotNull(ext.getReferenceServiceLife());
		assertEquals(50d, ext.getReferenceServiceLife().getYears(), 1e-9);
	}

	@Test
	public void keepsScenarioData() {
		var epd = new Process();
		Epds.withScenarioData(epd)
				.withUseStageData()
				.add(new EpdUseStageData());

		Cleanup.on(epd);

		var ext = Epds.getInfoExtension(epd);
		assertNotNull(ext);
		assertNotNull(ext.getScenarioData());
		assertEquals(1, ext.getScenarioData().getUseStageData().size());
	}

	@Test
	public void keepsSvhcDeclaration() {
		var epd = new Process();
		Epds.withSvhc(epd).withPresent(false);

		Cleanup.on(epd);

		var ext = Epds.getInfoExtension(epd);
		assertNotNull(ext);
		assertNotNull(ext.getSvhc());
		assertFalse(ext.getSvhc().isPresent());
	}

	@Test
	public void removesEmptyServiceLifeWithOtherData() {
		var epd = new Process();
		Epds.withReferenceServiceLife(epd);
		Epds.withProductIds(epd).add(new EpdProductId()
				.withType("GTIN")
				.withValue("123"));

		Cleanup.on(epd);

		var ext = Epds.getInfoExtension(epd);
		assertNotNull(ext);
		assertNull(ext.getReferenceServiceLife());
		assertEquals(1, ext.getProductIds().size());
	}

	@Test
	public void removesEmptyInfoExtension() {
		var epd = new Process();
		Epds.withReferenceServiceLife(epd);

		Cleanup.on(epd);

		assertNull(Epds.getInfoExtension(epd));
	}
}
