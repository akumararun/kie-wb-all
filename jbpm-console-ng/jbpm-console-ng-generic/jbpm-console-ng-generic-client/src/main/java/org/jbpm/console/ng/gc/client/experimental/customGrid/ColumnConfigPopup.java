package org.jbpm.console.ng.gc.client.experimental.customGrid;

import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.client.common.popups.footers.ModalFooterOKButton;

import java.util.Map;

public class ColumnConfigPopup extends Modal {

    interface ColumnConfigPopupUIBinder
            extends UiBinder<Widget, ColumnConfigPopup> {
    };

    private static ColumnConfigPopupUIBinder uiBinder = GWT.create(ColumnConfigPopupUIBinder.class);

    @UiField
    VerticalPanel columnPopupMainPanel;

    private DataGrid dataGrid;
    private GridColumnsHelper gridColumnsHelper;
    private GridColumnsConfig gridColumnsConfig;

    public ColumnConfigPopup( DataGrid<?> dataGrid ) {

        this.dataGrid = dataGrid;
        gridColumnsHelper = new GridColumnsHelper(dataGrid);

        setTitle( "Configure grid columns" );
        setMaxHeigth( ( Window.getClientHeight() * 0.75 ) + "px" );
        setBackdrop( BackdropType.STATIC );
        setKeyboard( true );
        setAnimation( true );
        setDynamicSafe(true);

        add(uiBinder.createAndBindUi(this));

        add( new ModalFooterOKButton(
                new Command() {
                    @Override
                    public void execute() {
                        gridColumnsHelper.saveGridColumnsConfig(gridColumnsConfig);
                        hide();
                    }
                }
        ) );
    }

    public void init( String gridId ) {
        // Initialize the popup when the widget's icon is clicked
        columnPopupMainPanel.clear();

        gridColumnsConfig = gridColumnsHelper.getColumnsConfigForGrid( gridId );

        for ( final Map.Entry<Integer, ColumnSettings> entry : gridColumnsConfig.entrySet() ) {

            final ColumnSettings columnSettings = entry.getValue();

            final CheckBox checkBox = new com.google.gwt.user.client.ui.CheckBox();
            checkBox.setValue(columnSettings.isVisible());
            checkBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick( ClickEvent event ) {
                    columnSettings.setVisible( checkBox.getValue() );
                    applyGridChange(checkBox.getValue(), entry.getKey());
                }
            });
            columnPopupMainPanel.add( new ColumnConfigRowWidget( checkBox, columnSettings.getColumnLabel()) );
        }
    }

    private void applyGridChange(boolean insert, int selectedColumnIndex) {
        if (!insert) {
            int removeIndex = gridColumnsHelper.notifyColumnRemoved(selectedColumnIndex);
            dataGrid.removeColumn( removeIndex );
        } else {
            int addIndex = gridColumnsHelper.notifyColumnAdded(selectedColumnIndex);
            dataGrid.insertColumn(addIndex,
                    gridColumnsHelper.getColumn(selectedColumnIndex),
                    gridColumnsHelper.getColumnHeader(selectedColumnIndex),
                    gridColumnsHelper.getColumnFooter(selectedColumnIndex));
            dataGrid.setColumnWidth( addIndex, gridColumnsHelper.getColumnWidth( selectedColumnIndex ) );
        }
        dataGrid.redraw();
    }
}
