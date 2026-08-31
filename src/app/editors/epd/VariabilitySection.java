package app.editors.epd;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
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
		var layout = (GridLayout) comp.getLayout();
		layout.numColumns = 4;

		var v = Epds.getVariability(epd);

		// Manufacturer variability row
		UI.formLabel(comp, tk, M.ManufacturerVariability);
		var mCombo = new ComboViewer(comp, SWT.READ_ONLY);
		UI.gridData(mCombo.getControl(), true, false);
		mCombo.setContentProvider(ArrayContentProvider.getInstance());
		mCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EpdManufacturerVariability.VariabilityType type) {
					return Labels.get(type);
				}
				return super.getText(element);
			}
		});
		mCombo.setInput(EpdManufacturerVariability.VariabilityType.values());
		var mInitType = (v != null && v.getManufacturerVariability() != null)
				? v.getManufacturerVariability().getType()
				: null;
		if (mInitType != null) {
			mCombo.setSelection(new StructuredSelection(mInitType));
		}
		mCombo.addSelectionChangedListener(e -> {
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

		// Manufacturer variability variation (%)
		var mInitVar = (v != null && v.getManufacturerVariability() != null)
				? v.getManufacturerVariability().getVariation()
				: null;
		DoubleText.on(editor, comp, tk)
				.withLabel(M.VariationPercent)
				.withInitial(mInitVar)
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

		// Manufacturer range
		UI.formLabel(comp, tk, M.VariationRange);
		var mRangeCombo = new ComboViewer(comp, SWT.READ_ONLY);
		UI.gridData(mRangeCombo.getControl(), true, false);
		mRangeCombo.setContentProvider(ArrayContentProvider.getInstance());
		mRangeCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EpdVariationRange range) {
					return Labels.get(range);
				}
				return super.getText(element);
			}
		});
		mRangeCombo.setInput(EpdVariationRange.values());
		var mInitRange = (v != null && v.getManufacturerVariability() != null)
				? v.getManufacturerVariability().getRange()
				: null;
		if (mInitRange != null) {
			mRangeCombo.setSelection(new StructuredSelection(mInitRange));
		}
		mRangeCombo.addSelectionChangedListener(e -> {
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

		// Empty layout cell for 4-columns layout alignment
		tk.createLabel(comp, "");
		tk.createLabel(comp, "");

		// Product variability row
		UI.formLabel(comp, tk, M.ProductVariability);
		var pCombo = new ComboViewer(comp, SWT.READ_ONLY);
		UI.gridData(pCombo.getControl(), true, false);
		pCombo.setContentProvider(ArrayContentProvider.getInstance());
		pCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EpdProductVariability.VariabilityType type) {
					return Labels.get(type);
				}
				return super.getText(element);
			}
		});
		pCombo.setInput(EpdProductVariability.VariabilityType.values());
		var pInitType = (v != null && v.getProductVariability() != null)
				? v.getProductVariability().getType()
				: null;
		if (pInitType != null) {
			pCombo.setSelection(new StructuredSelection(pInitType));
		}
		pCombo.addSelectionChangedListener(e -> {
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

		// Product variability variation (%)
		var pInitVar = (v != null && v.getProductVariability() != null)
				? v.getProductVariability().getVariation()
				: null;
		DoubleText.on(editor, comp, tk)
				.withLabel(M.VariationPercent)
				.withInitial(pInitVar)
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

		// Product range
		UI.formLabel(comp, tk, M.VariationRange);
		var pRangeCombo = new ComboViewer(comp, SWT.READ_ONLY);
		UI.gridData(pRangeCombo.getControl(), true, false);
		pRangeCombo.setContentProvider(ArrayContentProvider.getInstance());
		pRangeCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EpdVariationRange range) {
					return Labels.get(range);
				}
				return super.getText(element);
			}
		});
		pRangeCombo.setInput(EpdVariationRange.values());
		var pInitRange = (v != null && v.getProductVariability() != null)
				? v.getProductVariability().getRange()
				: null;
		if (pInitRange != null) {
			pRangeCombo.setSelection(new StructuredSelection(pInitRange));
		}
		pRangeCombo.addSelectionChangedListener(e -> {
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

		// Empty layout cell for 4-columns layout alignment
		tk.createLabel(comp, "");
		tk.createLabel(comp, "");

		var descriptions = v != null ? v.getDescriptions() : null;
		LangText.builder(editor, tk)
				.nextMulti(M.VariabilityDescription)
				.val(descriptions)
				.edit(() -> Epds.withVariability(epd).withDescriptions())
				.draw(comp);
	}
}
