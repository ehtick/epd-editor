package app.editors.epd;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdManufacturerVariability;
import org.openlca.ilcd.processes.epd.EpdProductVariability;
import org.openlca.ilcd.processes.epd.EpdVariationRange;
import org.openlca.ilcd.util.Epds;

import app.M;
import app.rcp.Labels;
import app.util.DoubleText;
import app.util.LangText;
import app.util.UI;
import app.util.Viewers;

class VariabilitySection {

	private final EpdEditor editor;
	private final Process epd;

	VariabilitySection(EpdEditor editor) {
		this.editor = editor;
		this.epd = editor.epd;
	}

	void render(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, M.Variability);
		UI.gridLayout(comp, 3);

		// two columns for manufacturer and product variability
		UI.filler(comp, tk);
		UI.formLabel(comp, tk, M.ManufacturerVariability);
		UI.formLabel(comp, tk, M.ProductVariability);
		var v = Epds.getVariability(epd);

		// type combos
		UI.formLabel(comp, tk, M.Type);
		var manuCombo = new ComboViewer(comp, SWT.READ_ONLY);
		UI.gridData(manuCombo.getControl(), true, false);
		manuCombo.setContentProvider(ArrayContentProvider.getInstance());
		manuCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object e) {
				return e instanceof EpdManufacturerVariability.VariabilityType type
					? Labels.get(type)
					: super.getText(e);
			}
		});
		manuCombo.setInput(EpdManufacturerVariability.VariabilityType.values());
		var manuInitType = (v != null && v.getManufacturerVariability() != null)
			? v.getManufacturerVariability().getType()
			: null;
		if (manuInitType != null) {
			manuCombo.setSelection(new StructuredSelection(manuInitType));
		}
		manuCombo.addSelectionChangedListener(e -> {
			EpdManufacturerVariability.VariabilityType type = Viewers.getFirst(e.getSelection());
			var variability = Epds.withVariability(epd);
			var mv = variability.getManufacturerVariability();
			if (mv == null) {
				mv = new EpdManufacturerVariability();
				variability.withManufacturerVariability(mv);
			}
			mv.withType(type);
			editor.setDirty();
		});

		var prodCombo = new ComboViewer(comp, SWT.READ_ONLY);
		UI.gridData(prodCombo.getControl(), true, false);
		prodCombo.setContentProvider(ArrayContentProvider.getInstance());
		prodCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object e) {
				return e instanceof EpdProductVariability.VariabilityType type
					? Labels.get(type)
					: super.getText(e);
			}
		});

		prodCombo.setInput(EpdProductVariability.VariabilityType.values());
		var pInitType = (v != null && v.getProductVariability() != null)
			? v.getProductVariability().getType()
			: null;
		if (pInitType != null) {
			prodCombo.setSelection(new StructuredSelection(pInitType));
		}
		prodCombo.addSelectionChangedListener(e -> {
			EpdProductVariability.VariabilityType type = Viewers.getFirst(e.getSelection());
			var variability = Epds.withVariability(epd);
			var pv = variability.getProductVariability();
			if (pv == null) {
				pv = new EpdProductVariability();
				variability.withProductVariability(pv);
			}
			pv.withType(type);
			editor.setDirty();
		});

		//  variation (%)
		UI.formLabel(comp, tk, M.VariationPercent);
		var manuVar = (v != null && v.getManufacturerVariability() != null)
			? v.getManufacturerVariability().getVariation()
			: null;
		DoubleText.on(editor, comp, tk)
			.withInitial(manuVar)
			.onChange(val -> {
				var variability = Epds.withVariability(epd);
				var mv = variability.getManufacturerVariability();
				if (mv == null) {
					mv = new EpdManufacturerVariability();
					variability.withManufacturerVariability(mv);
				}
				mv.withVariation(val);
			})
			.render();

		var prodVar = (v != null && v.getProductVariability() != null)
			? v.getProductVariability().getVariation()
			: null;
		DoubleText.on(editor, comp, tk)
			.withInitial(prodVar)
			.onChange(val -> {
				var variability = Epds.withVariability(epd);
				var pv = variability.getProductVariability();
				if (pv == null) {
					pv = new EpdProductVariability();
					variability.withProductVariability(pv);
				}
				pv.withVariation(val);
			})
			.render();

		// range
		UI.formLabel(comp, tk, M.VariationRange);
		var manuRangeCombo = new ComboViewer(comp, SWT.READ_ONLY);
		UI.gridData(manuRangeCombo.getControl(), true, false);
		manuRangeCombo.setContentProvider(ArrayContentProvider.getInstance());
		manuRangeCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return element instanceof EpdVariationRange range
					? Labels.get(range)
					: super.getText(element);
			}
		});
		manuRangeCombo.setInput(EpdVariationRange.values());
		var manuInitRange = (v != null && v.getManufacturerVariability() != null)
			? v.getManufacturerVariability().getRange()
			: null;
		if (manuInitRange != null) {
			manuRangeCombo.setSelection(new StructuredSelection(manuInitRange));
		}
		manuRangeCombo.addSelectionChangedListener(e -> {
			EpdVariationRange range = Viewers.getFirst(e.getSelection());
			var variability = Epds.withVariability(epd);
			var mv = variability.getManufacturerVariability();
			if (mv == null) {
				mv = new EpdManufacturerVariability();
				variability.withManufacturerVariability(mv);
			}
			mv.withRange(range);
			editor.setDirty();
		});

		var prodRangeCombo = new ComboViewer(comp, SWT.READ_ONLY);
		UI.gridData(prodRangeCombo.getControl(), true, false);
		prodRangeCombo.setContentProvider(ArrayContentProvider.getInstance());
		prodRangeCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EpdVariationRange range) {
					return Labels.get(range);
				}
				return super.getText(element);
			}
		});
		prodRangeCombo.setInput(EpdVariationRange.values());
		var pInitRange = (v != null && v.getProductVariability() != null)
			? v.getProductVariability().getRange()
			: null;
		if (pInitRange != null) {
			prodRangeCombo.setSelection(new StructuredSelection(pInitRange));
		}
		prodRangeCombo.addSelectionChangedListener(e -> {
			EpdVariationRange range = Viewers.getFirst(e.getSelection());
			var variability = Epds.withVariability(epd);
			var pv = variability.getProductVariability();
			if (pv == null) {
				pv = new EpdProductVariability();
				variability.withProductVariability(pv);
			}
			pv.withRange(range);
			editor.setDirty();
		});

		UI.formLabel(comp, tk, M.VariabilityDescription);
		var textComp = tk.createComposite(comp);
		UI.gridData(textComp, true, false).horizontalSpan = 2;
		UI.innerGrid(textComp, 2);

		var descriptions = v != null ? v.getDescriptions() : null;
		LangText.builder(editor, tk)
			.nextMulti("")
			.val(descriptions)
			.edit(() -> Epds.withVariability(epd).withDescriptions())
			.draw(textComp);
	}
}
