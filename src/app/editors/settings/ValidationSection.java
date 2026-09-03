package app.editors.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.commons.Strings;

import app.M;
import app.rcp.Icon;
import app.store.validation.ValidationProfiles;
import app.util.Actions;
import app.util.FileChooser;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

class ValidationSection {

	private final SettingsPage page;
	private TableViewer table;
	private final List<ProfileInfo> infos = new ArrayList<>();

	ValidationSection(SettingsPage page) {
		this.page = page;
		for (File file : ValidationProfiles.getFiles()) {
			infos.add(ProfileInfo.of(file));
		}
		infos.sort((i1, i2) -> Strings
				.compareIgnoreCase(i1.name(), i2.name()));
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, M.ValidationProfiles);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, M.Name, M.Version, M.File);
		table.setLabelProvider(new ProfileLabel());
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		bindActions(section);
		table.setInput(infos);
	}

	private void bindActions(Section section) {
		Action ref = Actions.create(M.SetAsActiveProfile, Icon.OK.des(),
			this::selectActive);
		Action add = Actions.create(M.Add, Icon.ADD.des(), this::add);
		Action del = Actions.create(M.Remove, Icon.DELETE.des(), this::remove);
		Action sync = Actions.create(M.SearchUpdates, Icon.RELOAD.des(), () -> {
			var synced = ProfileSyncDialog.sync().orElse(null);
			if (synced == null)
				return;
			infos.clear();
			infos.addAll(synced);
			table.setInput(synced);
		});
		Actions.bind(table, ref, add, del, sync);
		Actions.bind(section, ref, add, del, sync);
	}

	private void add() {
		File file = FileChooser.open("*.jar");
		if (file == null)
			return;
		if (ValidationProfiles.contains(file)) {
			MsgBox.error(M.AlreadyExists,
				"A profile with this name already exists.");
			return;
		}
		file = ValidationProfiles.put(file);
		if (file == null)
			return;
		infos.add(ProfileInfo.of(file));
		table.setInput(infos);
	}

	private void remove() {
		ProfileInfo info = Viewers.getFirstSelected(table);
		if (info == null)
			return;
		boolean b = MsgBox.ask(M.Delete,
			"Delete selected validation profile?");
		if (!b)
			return;
		infos.remove(info);
		if (Strings.equalsIgnoreCase(page.settings.validationProfile,
			info.file().getName())) {
			page.settings.validationProfile = null;
			page.setDirty();
		}
		info.file().delete();
		table.setInput(infos);
	}

	private void selectActive() {
		ProfileInfo info = Viewers.getFirstSelected(table);
		if (info == null)
			return;
		page.settings.validationProfile = info.file().getName();
		page.setDirty();
		table.refresh();
	}

	private class ProfileLabel extends LabelProvider
		implements ITableLabelProvider, ITableFontProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof ProfileInfo pi))
				return null;
			return switch (col) {
				case 0 -> pi.name();
				case 1 -> pi.version();
				case 2 -> pi.file().getName();
				default -> null;
			};
		}

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof ProfileInfo pi))
				return null;
			if (Strings.equalsIgnoreCase(page.settings.validationProfile,
				pi.file().getName()))
				return UI.boldFont();
			return null;
		}
	}
}
