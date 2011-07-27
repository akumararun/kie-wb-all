/*
 * Copyright 2011 JBoss Inc
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
package org.drools.guvnor.server;

import com.google.gwt.user.client.rpc.SerializationException;
import org.drools.guvnor.client.rpc.*;
import org.drools.guvnor.server.cache.RuleBaseCache;
import org.drools.guvnor.server.contenthandler.ContentHandler;
import org.drools.guvnor.server.contenthandler.ContentManager;
import org.drools.guvnor.server.contenthandler.ICanHasAttachment;
import org.drools.guvnor.server.repository.UserInbox;
import org.drools.guvnor.server.security.CategoryPathType;
import org.drools.guvnor.server.security.PackageNameType;
import org.drools.guvnor.server.security.PackageUUIDType;
import org.drools.guvnor.server.security.RoleType;
import org.drools.guvnor.server.util.Discussion;
import org.drools.guvnor.server.util.LoggingHelper;
import org.drools.guvnor.server.util.RuleAssetPopulator;
import org.drools.repository.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Name("org.drools.guvnor.client.rpc.AssetService")
@AutoCreate
public class RepositoryAssetService
        implements
        AssetService {

    @In
    private RulesRepository repository;

    private static final long serialVersionUID = 90111;

    private static final LoggingHelper log = LoggingHelper.getLogger(RepositoryAssetService.class);

    private final ServiceSecurity serviceSecurity = new ServiceSecurity();

    private final RepositoryAssetOperations repositoryAssetOperations = new RepositoryAssetOperations();

    @Create
    public void create() {
        repositoryAssetOperations.setRulesRepository(getRulesRepository());
    }

    /* This is called in hosted mode when creating "by hand" */
    public void setRulesRepository(RulesRepository repository) {
        this.repository = repository;
        create();
    }

    public RulesRepository getRulesRepository() {
        return repository;
    }

    /**
     * This actually does the hard work of loading up an asset based on its
     * format.
     * <p/>
     * Role-based Authorization check: This method can be accessed if user has
     * following permissions: 1. The user has a ANALYST_READ role or higher
     * (i.e., ANALYST) and this role has permission to access the category which
     * the asset belongs to. Or. 2. The user has a package.readonly role or
     * higher (i.e., package.admin, package.developer) and this role has
     * permission to access the package which the asset belongs to.
     */
    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public RuleAsset loadRuleAsset(String uuid) throws SerializationException {

        long time = System.currentTimeMillis();

        AssetItem item = getRulesRepository().loadAssetByUUID(uuid);
        RuleAsset asset = new RuleAssetPopulator().populateFrom(item);

        asset.setMetaData(repositoryAssetOperations.populateMetaData(item));


        if (Contexts.isSessionContextActive()) {
            try {
                serviceSecurity.checkSecurityIsPackageReadOnlyWithPackageName(asset.getMetaData().getPackageName());
            } catch (RuntimeException e) {
                handleExceptionAndVerifyCategoryBasedPermission(asset);
            }
        }

        PackageItem pkgItem = handlePackageItem(item,
                asset);

        log.debug("Package: " + pkgItem.getName() + ", asset: " + item.getName() + ". Load time taken for asset: " + (System.currentTimeMillis() - time));
        UserInbox.recordOpeningEvent(item);
        return asset;

    }

    private PackageItem handlePackageItem(AssetItem item,
                                          RuleAsset asset) throws SerializationException {
        PackageItem packageItem = item.getPackage();

        ContentHandler handler = ContentManager.getHandler(asset.getMetaData().getFormat());
        handler.retrieveAssetContent(asset,
                item);

        asset.setReadonly(asset.getMetaData().isHasSucceedingVersion());

        if (packageItem.isSnapshot()) {
            asset.setReadonly(true);
        }
        return packageItem;
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public RuleAsset[] loadRuleAssets(String[] uuids) throws SerializationException {
        return loadRuleAssets(Arrays.asList(uuids));
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    /**
     *
     * Role-based Authorization check: This method can be accessed if user has
     * following permissions:
     * 1. The user has a Analyst role and this role has permission to access the category
     * which the asset belongs to.
     * Or.
     * 2. The user has a package.developer role or higher (i.e., package.admin)
     * and this role has permission to access the package which the asset belongs to.
     */
    public String checkinVersion(RuleAsset asset) throws SerializationException {

        // Verify if the user has permission to access the asset through package
        // based permission.
        // If failed, then verify if the user has permission to access the asset
        // through category
        // based permission
        if (Contexts.isSessionContextActive()) {
            boolean passed = false;

            try {
                Identity.instance().checkPermission(new PackageNameType(asset.getMetaData().getPackageName()),
                        RoleType.PACKAGE_DEVELOPER.getName());
            } catch (RuntimeException e) {
                if (asset.getMetaData().getCategories().length == 0) {
                    Identity.instance().checkPermission(new CategoryPathType(null),
                            RoleType.ANALYST.getName());
                } else {
                    RuntimeException exception = null;

                    for (String cat : asset.getMetaData().getCategories()) {
                        try {
                            Identity.instance().checkPermission(new CategoryPathType(cat),
                                    RoleType.ANALYST.getName());
                            passed = true;
                        } catch (RuntimeException re) {
                            exception = re;
                        }
                    }
                    if (!passed) {
                        throw exception;
                    }
                }
            }
        }

        log.info("USER:" + getCurrentUserName() + " CHECKING IN asset: [" + asset.getName() + "] UUID: [" + asset.getUuid() + "] ");
        return repositoryAssetOperations.checkinVersion(asset);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public void restoreVersion(String versionUUID,
                               String assetUUID,
                               String comment) {
        repositoryAssetOperations.restoreVersion(versionUUID,
                assetUUID,
                comment);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public TableDataResult loadItemHistory(String uuid) throws SerializationException {
        //VersionableItem assetItem = getRulesRepository().loadAssetByUUID( uuid );
        VersionableItem assetItem = getRulesRepository().loadItemByUUID(uuid);

        //serviceSecurity.checkSecurityAssetPackagePackageReadOnly( assetItem );
        return repositoryAssetOperations.loadItemHistory(assetItem);
    }

    /**
     * @deprecated in favour of {@link #loadArchivedAssets(PageRequest)}
     */
    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public TableDataResult loadAssetHistory(String packageUUID,
                                            String assetName) throws SerializationException {
        PackageItem pi = getRulesRepository().loadPackageByUUID(packageUUID);
        AssetItem assetItem = pi.loadAsset(assetName);
        serviceSecurity.checkSecurityPackageReadOnlyWithPackageUuid(assetItem.getPackage().getUUID());

        return repositoryAssetOperations.loadItemHistory(assetItem);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    @Deprecated
    public TableDataResult loadArchivedAssets(int skip,
                                              int numRows) throws SerializationException {
        return repositoryAssetOperations.loadArchivedAssets(skip,
                numRows);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public PageResponse<AdminArchivedPageRow> loadArchivedAssets(PageRequest request) throws SerializationException {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (request.getPageSize() != null && request.getPageSize() < 0) {
            throw new IllegalArgumentException("pageSize cannot be less than zero.");
        }

        return repositoryAssetOperations.loadArchivedAssets(request);
    }

    /**
     * @deprecated in favour of {@link #findAssetPage(AssetPageRequest)}
     */
    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public TableDataResult listAssetsWithPackageName(String packageName,
                                                     String formats[],
                                                     int skip,
                                                     int numRows,
                                                     String tableConfig) throws SerializationException {
        PackageItem pkg = getRulesRepository().loadPackage(packageName);
        return listAssets(pkg.getUUID(),
                formats,
                skip,
                numRows,
                tableConfig);
    }

    /**
     * @deprecated in favour of {@link #findAssetPage(AssetPageRequest)}
     */
    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public TableDataResult listAssets(String packageUuid,
                                      String formats[],
                                      int skip,
                                      int numRows,
                                      String tableConfig) throws SerializationException {
        log.debug("Loading asset list for [" + packageUuid + "]");
        if (numRows == 0) {
            throw new DetailedSerializationException("Unable to return zero results (bug)",
                    "probably have the parameters around the wrong way, sigh...");
        }
        return repositoryAssetOperations.listAssets(packageUuid,
                formats,
                skip,
                numRows,
                tableConfig);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public String copyAsset(String assetUUID,
                            String newPackage,
                            String newName) {
        serviceSecurity.checkSecurityIsPackageDeveloperWithPackageName(newPackage);

        log.info("USER:" + getCurrentUserName() + " COPYING asset: [" + assetUUID + "] to [" + newName + "] in PACKAGE [" + newPackage + "]");
        return getRulesRepository().copyAsset(assetUUID,
                newPackage,
                newName);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public void changeAssetPackage(String uuid,
                                   String newPackage,
                                   String comment) {
        serviceSecurity.checkSecurityIsPackageDeveloperWithPackageName(newPackage);

        log.info("USER:" + getCurrentUserName() + " CHANGING PACKAGE OF asset: [" + uuid + "] to [" + newPackage + "]");
        getRulesRepository().moveRuleItemPackage(newPackage,
                uuid,
                comment);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public void promoteAssetToGlobalArea(String uuid) {
        if (Contexts.isSessionContextActive()) {
            Identity.instance().checkPermission(new PackageNameType(RulesRepository.RULE_GLOBAL_AREA),
                    RoleType.PACKAGE_DEVELOPER.getName());
        }

        log.info("USER:" + getCurrentUserName() + " CHANGING PACKAGE OF asset: [" + uuid + "] to [ globalArea ]");
        getRulesRepository().moveRuleItemPackage(RulesRepository.RULE_GLOBAL_AREA,
                uuid,
                "promote asset to globalArea");
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public String buildAssetSource(RuleAsset asset) throws SerializationException {
        serviceSecurity.checkSecurityIsPackageDeveloperWithPackageName(asset.getMetaData().getPackageName());
        return repositoryAssetOperations.buildAssetSource(asset);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public String renameAsset(String uuid,
                              String newName) {
        AssetItem item = getRulesRepository().loadAssetByUUID(uuid);
        serviceSecurity.checkSecurityIsPackageDeveloperWithPackageUuid(item.getPackage().getUUID());

        return repositoryAssetOperations.renameAsset(uuid,
                newName);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public void archiveAsset(String uuid) {
        archiveOrUnarchiveAsset(uuid,
                true);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public BuilderResult validateAsset(RuleAsset asset) throws SerializationException {
        serviceSecurity.checkSecurityIsPackageDeveloperWithPackageName(asset.getMetaData().getPackageName());
        return repositoryAssetOperations.validateAsset(asset);
    }

    public void unArchiveAsset(String uuid) {
        archiveOrUnarchiveAsset(uuid,
                false);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public void archiveAssets(String[] uuids,
                              boolean value) {
        for (String uuid : uuids) {
            archiveOrUnarchiveAsset(uuid,
                    value);
        }
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public void removeAsset(String uuid) {
        try {
            AssetItem item = getRulesRepository().loadAssetByUUID(uuid);
            serviceSecurity.checkSecurityIsPackageDeveloperWithPackageUuid(item.getPackage().getUUID());

            item.remove();
            getRulesRepository().save();
        } catch (RulesRepositoryException e) {
            log.error("Unable to remove asset.",
                    e);
            throw e;
        }
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public void removeAssets(String[] uuids) {
        for (String uuid : uuids) {
            removeAsset(uuid);
        }
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public PageResponse<AssetPageRow> findAssetPage(AssetPageRequest request) throws SerializationException {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (request.getPageSize() != null && request.getPageSize() < 0) {
            throw new IllegalArgumentException("pageSize cannot be less than zero.");
        }

        return repositoryAssetOperations.findAssetPage(request);
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public PageResponse<QueryPageRow> quickFindAsset(QueryPageRequest request) throws SerializationException {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (request.getPageSize() != null && request.getPageSize() < 0) {
            throw new IllegalArgumentException("pageSize cannot be less than zero.");
        }

        return repositoryAssetOperations.quickFindAsset(request);
    }

    /**
     * @deprecated in favour of {@link #quickFindAsset(QueryPageRequest)}
     */
    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public TableDataResult quickFindAsset(String searchText,
                                          boolean searchArchived,
                                          int skip,
                                          int numRows) throws SerializationException {
        return repositoryAssetOperations.quickFindAsset(searchText,
                searchArchived,
                skip,
                numRows);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.drools.guvnor.client.rpc.RepositoryService#lockAsset(java.lang.String
     * )
     */
    @Restrict("#{identity.loggedIn}")
    public void lockAsset(String uuid) {
        repositoryAssetOperations.lockAsset(uuid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.drools.guvnor.client.rpc.RepositoryService#unLockAsset(java.lang.
     * String)
     */
    @Restrict("#{identity.loggedIn}")
    public void unLockAsset(String uuid) {
        repositoryAssetOperations.unLockAsset(uuid);
    }

    /**
     * @deprecated in favour of {@link ServiceImplementation#queryFullText(QueryPageRequest)}
     */
    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public TableDataResult queryFullText(String text,
                                         boolean seekArchived,
                                         int skip,
                                         int numRows) throws SerializationException {
        if (numRows == 0) {
            throw new DetailedSerializationException("Unable to return zero results (bug)",
                    "probably have the parameters around the wrong way, sigh...");
        }
        return repositoryAssetOperations.queryFullText(text,
                seekArchived,
                skip,
                numRows);
    }

    private void handleExceptionAndVerifyCategoryBasedPermission(RuleAsset asset) {
        if (asset.getMetaData().getCategories().length == 0) {
            serviceSecurity.checkPermissionAnalystReadWithCategoryPathType(null);
        } else {
            RuntimeException exception = null;
            boolean passed = false;
            for (String category : asset.getMetaData().getCategories()) {
                try {
                    serviceSecurity.checkPermissionAnalystReadWithCategoryPathType(category);
                    passed = true;
                } catch (RuntimeException re) {
                    exception = re;
                }
            }
            if (!passed) {
                throw exception;
            }
        }
    }

    RuleAsset[] loadRuleAssets(Collection<String> uuids) throws SerializationException {
        if (uuids == null) {
            return null;
        }
        Collection<RuleAsset> assets = new HashSet<RuleAsset>();

        for (String uuid : uuids) {
            assets.add(loadRuleAsset(uuid));
        }

        return assets.toArray(new RuleAsset[assets.size()]);
    }

    private void archiveOrUnarchiveAsset(String uuid,
                                         boolean archive) {
        try {
            AssetItem item = getRulesRepository().loadAssetByUUID(uuid);

            serviceSecurity.checkSecurityIsPackageDeveloperWithPackageUuid(item.getPackage().getUUID());

            if (item.getPackage().isArchived()) {
                throw new RulesRepositoryException("The package [" + item.getPackageName() + "] that asset [" + item.getName() + "] belongs to is archived. You need to unarchive it first.");
            }

            log.info("USER:" + getCurrentUserName() + " ARCHIVING asset: [" + item.getName() + "] UUID: [" + item.getUUID() + "] ");

            try {
                ContentHandler handler = getContentHandler(item);
                if (handler instanceof ICanHasAttachment) {
                    ((ICanHasAttachment) handler).onAttachmentRemoved(item);
                }
            } catch (IOException e) {
                log.error("Unable to remove asset attachment",
                        e);
            }

            item.archiveItem(archive);
            PackageItem pkg = item.getPackage();
            pkg.updateBinaryUpToDate(false);
            RuleBaseCache.getInstance().remove(pkg.getUUID());
            if (archive) {
                item.checkin("archived");
            } else {
                item.checkin("unarchived");
            }

            push("packageChange",
                    pkg.getName());

        } catch (RulesRepositoryException e) {
            log.error("Unable to get item format.",
                    e);
            throw e;
        }
    }

    @Restrict("#{identity.loggedIn}")
    public List<DiscussionRecord> addToDiscussionForAsset(String assetId,
                                                          String comment) {
        return repositoryAssetOperations.addToDiscussionForAsset(assetId,
                comment);
    }

    @Restrict("#{identity.loggedIn}")
    public void clearAllDiscussionsForAsset(final String assetId) {
        serviceSecurity.checkSecurityIsAdmin();
        repositoryAssetOperations.clearAllDiscussionsForAsset(assetId);
    }

    @Restrict("#{identity.loggedIn}")
    public List<DiscussionRecord> loadDiscussionForAsset(String assetId) {
        return new Discussion().fromString(getRulesRepository().loadAssetByUUID(assetId).getStringProperty(Discussion.DISCUSSION_PROPERTY_KEY));
    }

    /**
     * Role-based Authorization check: This method can be accessed if user has
     * following permissions: 1. The user has a Analyst role and this role has
     * permission to access the category which the asset belongs to. Or. 2. The
     * user has a package.developer role or higher (i.e., package.admin) and
     * this role has permission to access the package which the asset belongs
     * to.
     */
    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public void changeState(String uuid,
                            String newState) {
        AssetItem asset = getRulesRepository().loadAssetByUUID(uuid);

        // Verify if the user has permission to access the asset through
        // package based permission.
        // If failed, then verify if the user has permission to access the
        // asset through category
        // based permission
        if (Contexts.isSessionContextActive()) {
            boolean passed = false;

            try {
                Identity.instance().checkPermission(new PackageUUIDType(asset.getPackage().getUUID()),
                        RoleType.PACKAGE_DEVELOPER.getName());
            } catch (RuntimeException e) {
                if (asset.getCategories().size() == 0) {
                    Identity.instance().checkPermission(new CategoryPathType(null),
                            RoleType.ANALYST.getName());
                } else {
                    RuntimeException exception = null;

                    for (CategoryItem cat : asset.getCategories()) {
                        try {
                            Identity.instance().checkPermission(new CategoryPathType(cat.getName()),
                                    RoleType.ANALYST.getName());
                            passed = true;
                        } catch (RuntimeException re) {
                            exception = re;
                        }
                    }
                    if (!passed) {
                        throw exception;
                    }
                }
            }
        }

        log.info("USER:" + getCurrentUserName() + " CHANGING ASSET STATUS. Asset name, uuid: " + "[" + asset.getName() + ", " + asset.getUUID() + "]" + " to [" + newState + "]");
        String oldState = asset.getStateDescription();
        asset.updateState(newState);

        push("statusChange",
                oldState);
        push("statusChange",
                newState);

        addToDiscussionForAsset(asset.getUUID(),
                oldState + " -> " + newState);

        getRulesRepository().save();
    }

    @WebRemote
    @Restrict("#{identity.loggedIn}")
    public void changePackageState(String uuid,
                                   String newState) {

        serviceSecurity.checkSecurityIsPackageDeveloperWithPackageUuid(uuid);

        PackageItem pkg = getRulesRepository().loadPackageByUUID(uuid);
        log.info("USER:" + getCurrentUserName() + " CHANGING Package STATUS. Asset name, uuid: " + "[" + pkg.getName() + ", " + pkg.getUUID() + "]" + " to [" + newState + "]");
        pkg.changeStatus(newState);

        getRulesRepository().save();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.drools.guvnor.client.rpc.RepositoryService#getAssetLockerUserName
     * (java.lang.String)
     */
    @Restrict("#{identity.loggedIn}")
    public String getAssetLockerUserName(String uuid) {
        return repositoryAssetOperations.getAssetLockerUserName(uuid);
    }

    private void push(String messageType,
                      String message) {
        Backchannel.getInstance().publish(new PushResponse(messageType,
                message));
    }

    private ContentHandler getContentHandler(AssetItem repoAsset) {
        return ContentManager.getHandler(repoAsset.getFormat());
    }

    private String getCurrentUserName() {
        return getRulesRepository().getSession().getUserID();
    }

}
