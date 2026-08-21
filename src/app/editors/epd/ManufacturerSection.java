package app.editors.epd;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.processes.epd.EpdManufacturer;
import org.openlca.ilcd.processes.epd.EpdSite;
import org.openlca.ilcd.util.Epds;

import app.App;
import app.M;
import app.editors.refs.RefLink;
import app.editors.refs.RefSelectionDialog;
import app.rcp.Icon;
import app.util.Actions;
import app.util.Controls;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;
import app.util.tables.ModifySupport;
import app.util.tables.TextModifier;

class ManufacturerSection {

	private static final String SITE = "Manufacturing site";
	private static final String FACILITY = "Facility identifier";
	private static final String STREET = "Street address";
	private static final String COUNTRY = "Country code";
	private static final String OLC = "OLC Location code";

	private final EpdEditor editor;
	private final List<EpdManufacturer> manufacturers;
	private Composite parent;
	private FormToolkit tk;

	ManufacturerSection(EpdEditor editor) {
		this.editor = editor;
		this.manufacturers = Epds.withManufacturers(editor.epd);
	}

	void render(Composite body, FormToolkit tk) {
		this.tk = tk;
		var section = UI.section(body, tk, M.Manufacturers);
		parent = UI.sectionClient(section, tk);
		UI.gridLayout(parent, 1);
		for (var m : manufacturers) {
			new SubSection(m);
		}
		var add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		Actions.bind(section, add);
		parent.layout(true, true);
	}

	private void add() {
		var ref = RefSelectionDialog.select(DataSetType.CONTACT);
		if (ref == null)
			return;
		var m = new EpdManufacturer().withContact(ref);
		manufacturers.add(m);
		new SubSection(m);
		parent.layout(true, true);
		editor.setDirty();
	}

	private class SubSection {

		private final EpdManufacturer m;
		private Section section;
		private TableViewer siteTable;

		SubSection(EpdManufacturer m) {
			this.m = m;
			createUi();
		}

		private void createUi() {
			var name = m.getContact() != null
				? App.s(m.getContact().getName())
				: "?";
			section = UI.section(parent, tk, name);
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);

			var top = tk.createComposite(comp);
			UI.gridData(top, true, false);
			UI.gridLayout(top, 2, 10, 0);
			contactRow(top);
			providingDataRow(top);
			createSiteTable(comp);

			var del = Actions.create(
				M.Remove, Icon.DELETE.des(), this::delete);
			Actions.bind(section, del);
		}

		private void contactRow(Composite comp) {
			UI.formLabel(comp, tk, "Contact");
			var link = new RefLink(comp, tk, DataSetType.CONTACT);
			link.setRef(m.getContact());
			link.onChange(ref -> {
				m.withContact(ref);
				section.setText(ref != null
					? App.s(ref.getName())
					: "?");
				editor.setDirty();
			});
		}

		private void providingDataRow(Composite comp) {
			var cb = UI.formCheckBox(comp, tk, "Is providing data");
			cb.setSelection(m.isProvidingData());
			Controls.onSelect(cb, _ -> {
				m.withProvidingData(cb.getSelection());
				editor.setDirty();
			});
		}

		private void createSiteTable(Composite comp) {
			siteTable = Tables.createViewer(
				comp, SITE, FACILITY, STREET, COUNTRY, OLC);
			siteTable.setLabelProvider(new SiteLabel());
			Tables.bindColumnWidths(siteTable, 0.2, 0.2, 0.2, 0.2, 0.2);
			UI.gridData(siteTable.getControl(), true, true).heightHint = 100;

			var ms = new ModifySupport<EpdSite>(siteTable);
			ms.bind(SITE, new SiteModifier(SITE))
				.bind(FACILITY, new SiteModifier(FACILITY))
				.bind(STREET, new SiteModifier(STREET))
				.bind(COUNTRY, new SiteModifier(COUNTRY))
				.bind(OLC, new SiteModifier(OLC));

			var add = Actions.create(M.Add, Icon.ADD.des(), this::addSite);
			var rem = Actions.create(M.Remove, Icon.DELETE.des(), this::removeSites);
			Actions.bind(siteTable, add, rem);
			siteTable.setInput(m.getSites());
		}

		private void addSite() {
			var site = new EpdSite().withName("New facility");
			m.withSites().add(site);
			siteTable.setInput(m.getSites());
			editor.setDirty();
		}

		private void removeSites() {
			var list = m.getSites();
			if (list.isEmpty())
				return;
			for (var s : Viewers.getAllSelected(siteTable)) {
				if (!(s instanceof EpdSite site))
					continue;
				list.remove(site);
			}
			siteTable.setInput(list);
			editor.setDirty();
		}

		private void delete() {
			manufacturers.remove(m);
			section.dispose();
			parent.layout(true, true);
			editor.setDirty();
		}
	}

	private static class SiteLabel extends BaseLabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object o, int i) {
			return null;
		}

		@Override
		public String getColumnText(Object o, int i) {
			if (!(o instanceof EpdSite s))
				return null;
			return switch (i) {
				case 0 -> s.getName();
				case 1 -> s.getFacilityIdentifier();
				case 2 -> s.getStreetAddress();
				case 3 -> s.getGeoCode();
				case 4 -> s.getOlc();
				default -> null;
			};
		}
	}

	private class SiteModifier extends TextModifier<EpdSite> {

		private final String field;

		SiteModifier(String field) {
			this.field = field;
		}

		@Override
		protected String getText(EpdSite site) {
			if (site == null)
				return null;
			return switch (field) {
				case SITE -> site.getName();
				case FACILITY -> site.getFacilityIdentifier();
				case STREET -> site.getStreetAddress();
				case COUNTRY -> site.getGeoCode();
				case OLC -> site.getOlc();
				default -> null;
			};
		}

		@Override
		protected void setText(EpdSite site, String text) {
			if (site == null)
				return;
			var old = getText(site);
			if (Objects.equals(old, text))
				return;
			switch (field) {
				case SITE -> site.withName(text);
				case FACILITY -> site.withFacilityIdentifier(text);
				case STREET -> site.withStreetAddress(text);
				case COUNTRY -> site.withGeoCode(text);
				case OLC -> site.withOlc(text);
			}
			editor.setDirty();
		}
	}
}
