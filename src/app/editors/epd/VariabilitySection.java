package app.editors.epd;

import java.util.ArrayList;
import java.util.Collections;

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

	private static final Object NONE = new Object();

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
					: "";
			}
		});
		manuCombo.setInput(nullable(EpdManufacturerVariability.VariabilityType.values()));
		var manuInitType = (v != null && v.getManufacturerVariability() != null)
			? v.getManufacturerVariability().getType()
			: null;
		manuCombo.setSelection(new StructuredSelection(manuInitType != null ? manuInitType : NONE));
		manuCombo.addSelectionChangedListener(e -> {
			Object selected = Viewers.getFirst(e.getSelection());
			EpdManufacturerVariability.VariabilityType type = selected instanceof EpdManufacturerVariability.VariabilityType t
				? t
				: null;
			var variability = Epds.withVariability(epd);
			var mv = variability.getManufacturerVariability();
			if (mv == null) {
				if (type == null)
					return;
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
					: "";
			}
		});

		prodCombo.setInput(nullable(EpdProductVariability.VariabilityType.values()));
		var pInitType = (v != null && v.getProductVariability() != null)
			? v.getProductVariability().getType()
			: null;
		prodCombo.setSelection(new StructuredSelection(pInitType != null ? pInitType : NONE));
		prodCombo.addSelectionChangedListener(e -> {
			Object selected = Viewers.getFirst(e.getSelection());
			EpdProductVariability.VariabilityType type = selected instanceof EpdProductVariability.VariabilityType t
				? t
				: null;
			var variability = Epds.withVariability(epd);
			var pv = variability.getProductVariability();
			if (pv == null) {
				if (type == null)
					return;
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
					: "";
			}
		});
		manuRangeCombo.setInput(nullable(EpdVariationRange.values()));
		var manuInitRange = (v != null && v.getManufacturerVariability() != null)
			? v.getManufacturerVariability().getRange()
			: null;
		manuRangeCombo.setSelection(new StructuredSelection(manuInitRange != null ? manuInitRange : NONE));
		manuRangeCombo.addSelectionChangedListener(e -> {
			Object selected = Viewers.getFirst(e.getSelection());
			EpdVariationRange range = selected instanceof EpdVariationRange r ? r : null;
			var variability = Epds.withVariability(epd);
			var mv = variability.getManufacturerVariability();
			if (mv == null) {
				if (range == null)
					return;
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
				return element instanceof EpdVariationRange range
					? Labels.get(range)
					: "";
			}
		});
		prodRangeCombo.setInput(nullable(EpdVariationRange.values()));
		var pInitRange = (v != null && v.getProductVariability() != null)
			? v.getProductVariability().getRange()
			: null;
		prodRangeCombo.setSelection(new StructuredSelection(pInitRange != null ? pInitRange : NONE));
		prodRangeCombo.addSelectionChangedListener(e -> {
			Object selected = Viewers.getFirst(e.getSelection());
			EpdVariationRange range = selected instanceof EpdVariationRange r ? r : null;
			var variability = Epds.withVariability(epd);
			var pv = variability.getProductVariability();
			if (pv == null) {
				if (range == null)
					return;
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

	private static <T> Object[] nullable(T[] values) {
		var list = new ArrayList<Object>();
		list.add(NONE);
		Collections.addAll(list, values);
		return list.toArray();
	}
}
