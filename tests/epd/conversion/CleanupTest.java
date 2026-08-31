package epd.conversion;

import static org.junit.Assert.*;

import javax.xml.datatype.DatatypeFactory;

import org.junit.Test;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdManufacturer;
import org.openlca.ilcd.processes.epd.EpdSite;
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
}
