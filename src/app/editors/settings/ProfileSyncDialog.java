package app.editors.settings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Strings;

import app.App;
import app.M;
import app.rcp.Icon;
import app.store.validation.MavenInfo;
import app.store.validation.ValidationProfiles;
import app.util.MsgBox;
import app.util.Tables;
import app.util.UI;
import app.util.Viewers;

@NullMarked
class ProfileSyncDialog extends FormDialog {

	private final List<MavenInfo> infos;
	private final Set<MavenInfo> selected = new HashSet<>();
	private @Nullable TableViewer table;

	static Optional<List<ProfileInfo>> sync() {

		var res = App.exec(
			"Search validation profiles ...",
			MavenInfo::fetchAll);
		if (res.isError()) {
			MsgBox.error("Search failed",
				"Failed to search for validation profiles: " + res.error());
			return Optional.empty();
		}

		var existing = ValidationProfiles.getFiles()
			.stream()
			.map(File::getName)
			.collect(Collectors.toSet());

		var newInfos = res.value().stream()
			.filter(info -> info.isForEpd() && !existing.contains(info.fileName()))
			.sorted((i, j) -> Strings.compareNatural(i.fileName(), j.fileName()))
			.toList();

		if (newInfos.isEmpty()) {
			MsgBox.info("No updates found",
				"No updated validation profiles were " +
					"found in the Central Maven Repository.");
			return Optional.empty();
		}

		var dialog = new ProfileSyncDialog(newInfos);
		if (dialog.open() != OK)
			return Optional.empty();

		var list = ValidationProfiles.getFiles().stream()
			.map(ProfileInfo::of)
			.sorted((i1, i2) -> Strings.compareIgnoreCase(i1.name(), i2.name()))
			.toList();

		return Optional.of(list);
	}

	private ProfileSyncDialog(List<MavenInfo> infos) {
		super(UI.shell());
		this.infos = infos;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(M.ValidationProfiles);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 400);
	}

	@Override
	protected void createButtonsForButtonBar(Composite comp) {
		createButton(comp, IDialogConstants.OK_ID, M.Download, false);
		var okBtn = getButton(IDialogConstants.OK_ID);
		if (okBtn != null) {
			okBtn.setEnabled(false);
		}
		createButton(comp, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var tk = mform.getToolkit();
		UI.formHeader(mform, M.ValidationProfiles);
		var body = UI.formBody(mform.getForm(), tk);
		UI.gridLayout(body, 1);

		table = Tables.createViewer(body, M.Name, M.Version, M.Download);
		table.setLabelProvider(new TableLabel());
		Tables.bindColumnWidths(table, 0.5, 0.3, 0.2);
		table.setInput(infos);

		Tables.onClick(table, _ -> {
			MavenInfo info = Viewers.getFirstSelected(table);
			if (info == null)
				return;
			if (selected.contains(info)) {
				selected.remove(info);
			} else {
				selected.add(info);
			}
			table.refresh();
			var downloadBtn = getButton(IDialogConstants.OK_ID);
			if (downloadBtn != null) {
				downloadBtn.setEnabled(!selected.isEmpty());
			}
		});
	}

	@Override
	protected void okPressed() {
		if (selected.isEmpty())
			return;

		String error = App.exec("Downloading validation profiles ...", () -> {
			try {
				var tempDir = Files.createTempDirectory("epd_editor_profiles");
				try {
					for (var info : selected) {
						var res = info.downloadTo(tempDir.toFile());
						if (res.isError()) {
							return "Failed to download " + info.fileName() + ": " + res.error();
						}
						var file = res.value();
						var target = ValidationProfiles.put(file);
						if (target == null) {
							return "Failed to save profile " + info.fileName();
						}
					}
					return null;
				} finally {
					try (var stream = Files.walk(tempDir)) {
						stream.sorted(Comparator.reverseOrder())
							.map(Path::toFile)
							.forEach(File::delete);
					} catch (Exception e) {
						// ignore cleanup errors
					}
				}
			} catch (Exception e) {
				return "Error downloading profiles: " + e.getMessage();
			}
		});

		if (error != null) {
			MsgBox.error("Download failed", error);
			return;
		}

		super.okPressed();
	}

	private class TableLabel extends LabelProvider implements ITableLabelProvider {

		@Nullable
		@Override
		public Image getColumnImage(Object obj, int col) {
			if (!(obj instanceof MavenInfo info) || col != 2)
				return null;
			return selected.contains(info)
				? Icon.CHECK_TRUE.img()
				: Icon.CHECK_FALSE.img();
		}

		@Nullable
		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof MavenInfo info))
				return null;
			return switch (col) {
				case 0 -> info.name();
				case 1 -> info.version();
				default -> null;
			};
		}
	}

}
