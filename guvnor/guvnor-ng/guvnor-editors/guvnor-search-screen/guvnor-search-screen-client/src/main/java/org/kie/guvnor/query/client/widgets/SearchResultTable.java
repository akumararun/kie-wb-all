/*
 * Copyright 2010 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.kie.guvnor.query.client.widgets;

import java.util.Collections;
import java.util.Date;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.kie.guvnor.commons.data.tables.PageResponse;
import org.kie.guvnor.commons.ui.client.tables.AbstractPathPagedTable;
import org.kie.guvnor.commons.ui.client.tables.CheckboxCellImpl;
import org.kie.guvnor.commons.ui.client.tables.ColumnPicker;
import org.kie.guvnor.commons.ui.client.tables.ComparableImageResource;
import org.kie.guvnor.commons.ui.client.tables.ComparableImageResourceCell;
import org.kie.guvnor.commons.ui.client.tables.SortableHeader;
import org.kie.guvnor.commons.ui.client.tables.SortableHeaderGroup;
import org.kie.guvnor.commons.ui.client.tables.TitledTextCell;
import org.kie.guvnor.commons.ui.client.tables.TitledTextColumn;
import org.kie.guvnor.query.client.resources.i18n.Constants;
import org.kie.guvnor.query.client.resources.images.ImageResources;
import org.kie.guvnor.query.model.QueryMetadataPageRequest;
import org.kie.guvnor.query.model.SearchPageRow;
import org.kie.guvnor.query.model.SearchTermPageRequest;
import org.kie.guvnor.query.service.SearchService;
import org.uberfire.client.workbench.type.ClientResourceType;
import org.uberfire.client.workbench.type.ClientTypeRegistry;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.*;

/**
 * Widget with a table of Assets.
 */
public class SearchResultTable extends AbstractPathPagedTable<SearchPageRow> {

    private static final int PAGE_SIZE = 10;

    private ClientTypeRegistry clientTypeRegistry = null;

    public SearchResultTable() {
        super( PAGE_SIZE );

        setDataProvider( new AsyncDataProvider<SearchPageRow>() {
            protected void onRangeChanged( HasData<SearchPageRow> display ) {
                updateRowCount( 0, true );
                updateRowData( 0, Collections.<SearchPageRow>emptyList() );
            }
        } );
    }

    public SearchResultTable( final QueryMetadataPageRequest queryRequest ) {
        super( PAGE_SIZE );

        if ( queryRequest.getPageSize() == null ) {
            queryRequest.setPageSize( PAGE_SIZE );
        }

        setDataProvider( new AsyncDataProvider<SearchPageRow>() {
            protected void onRangeChanged( HasData<SearchPageRow> display ) {
                queryRequest.setStartRowIndex( pager.getPageStart() );
                queryRequest.setPageSize( pageSize );

                createCall( new RemoteCallback<PageResponse<SearchPageRow>>() {
                    public void callback( final PageResponse<SearchPageRow> response ) {

                        updateRowCount( response.getTotalRowSize(),
                                        response.isTotalRowSizeExact() );
                        updateRowData( response.getStartRowIndex(),
                                       response.getPageRowList() );
                    }
                }, SearchService.class ).queryMetadata( queryRequest );
            }
        } );
    }

    public SearchResultTable( final SearchTermPageRequest searchRequest ) {
        super( PAGE_SIZE );

        if ( searchRequest.getPageSize() == null ) {
            searchRequest.setPageSize( PAGE_SIZE );
        }

        setDataProvider( new AsyncDataProvider<SearchPageRow>() {
            protected void onRangeChanged( HasData<SearchPageRow> display ) {
                searchRequest.setStartRowIndex( pager.getPageStart() );
                searchRequest.setPageSize( pageSize );

                createCall( new RemoteCallback<PageResponse<SearchPageRow>>() {
                    public void callback( final PageResponse<SearchPageRow> response ) {
                        updateRowCount( response.getTotalRowSize(),
                                        response.isTotalRowSizeExact() );
                        updateRowData( response.getStartRowIndex(),
                                       response.getPageRowList() );
                    }
                }, SearchService.class ).fullTextSearch( searchRequest );
            }
        } );
    }

    @Override
    protected void addAncillaryColumns( final ColumnPicker<SearchPageRow> columnPicker,
                                        final SortableHeaderGroup<SearchPageRow> sortableHeaderGroup ) {

        final Column<SearchPageRow, ComparableImageResource> formatColumn = new Column<SearchPageRow, ComparableImageResource>( new ComparableImageResourceCell() ) {

            public ComparableImageResource getValue( SearchPageRow row ) {
                final ClientResourceType associatedType = getClientTypeRegistry().resolve( row.getPath() );

                final Image icon;
                if ( associatedType.getIcon() == null || !( associatedType.getIcon() instanceof Image ) ) {
                    icon = new Image( ImageResources.INSTANCE.file() );
                } else {
                    icon = (Image) associatedType.getIcon();
                }

                return new ComparableImageResource( associatedType.getShortName(), icon );
            }
        };

        columnPicker.addColumn( formatColumn,
                                new SortableHeader<SearchPageRow, ComparableImageResource>(
                                        sortableHeaderGroup,
                                        Constants.INSTANCE.Format(),
                                        formatColumn ),
                                true );

        final TitledTextColumn<SearchPageRow> titleColumn = new TitledTextColumn<SearchPageRow>() {
            public TitledTextCell.TitledText getValue( SearchPageRow row ) {
                return new TitledTextCell.TitledText( row.getPath().getFileName(),
                                                      row.getAbbreviatedDescription() );
            }
        };
        columnPicker.addColumn( titleColumn,
                                new SortableHeader<SearchPageRow, TitledTextCell.TitledText>(
                                        sortableHeaderGroup,
                                        Constants.INSTANCE.Name(),
                                        titleColumn ),
                                true );

        final Column<SearchPageRow, Date> createdDateColumn = new Column<SearchPageRow, Date>(
                new DateCell( DateTimeFormat.getFormat( DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM ) ) ) {
            public Date getValue( SearchPageRow row ) {
                return row.getCreatedDate();
            }
        };
        columnPicker.addColumn( createdDateColumn,
                                new SortableHeader<SearchPageRow, Date>(
                                        sortableHeaderGroup,
                                        Constants.INSTANCE.CreatedDate(),
                                        createdDateColumn ),
                                false );

        final Column<SearchPageRow, Date> lastModifiedColumn = new Column<SearchPageRow, Date>(
                new DateCell( DateTimeFormat.getFormat( DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM ) ) ) {
            public Date getValue( final SearchPageRow row ) {
                return row.getLastModified();
            }
        };
        columnPicker.addColumn( lastModifiedColumn,
                                new SortableHeader<SearchPageRow, Date>(
                                        sortableHeaderGroup,
                                        Constants.INSTANCE.LastModified(),
                                        lastModifiedColumn ),
                                true );

        final Column<SearchPageRow, Boolean> isDisabledColumn = new Column<SearchPageRow, Boolean>( new CheckboxCellImpl( true ) ) {
            public Boolean getValue( final SearchPageRow row ) {
                return row.isDisabled();
            }
        };
        columnPicker.addColumn( isDisabledColumn,
                                new SortableHeader<SearchPageRow, Boolean>(
                                        sortableHeaderGroup,
                                        Constants.INSTANCE.Disabled(),
                                        isDisabledColumn ),
                                false );

    }

    private ClientTypeRegistry getClientTypeRegistry() {
        if ( clientTypeRegistry == null ) {
            clientTypeRegistry = IOC.getBeanManager().lookupBean( ClientTypeRegistry.class ).getInstance();
        }
        return clientTypeRegistry;
    }
}
