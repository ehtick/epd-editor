package app.editors.settings;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;
import org.openlca.commons.Strings;

import app.App;
import app.store.validation.MavenInfo;
import app.store.validation.ValidationProfiles;
import app.util.MsgBox;

@NullMarked
class ProfileSyncDialog {

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

		// TODO
		// now: open the FormDialog, present the new validation profiles in a table
		// with these columns: Name | Version | Download. The Download column should
		// show checkboxes. Only when at least one checkbox was clicked the Download
		// button of the dialog should be active.
		// Yes, instead of a OK button, there should be Download button with the
		// Cancel button; better: the label of the OK button should be "Download".

		// Thus, when OK was pressed: download the selected files to a temporary folder
		// first, when the download was successful move them to the folder of the
		// validation profiles

		// Then read all profiles information from that folder and update the
		// ValidationSection finally.


		return Optional.empty();
	}


}
