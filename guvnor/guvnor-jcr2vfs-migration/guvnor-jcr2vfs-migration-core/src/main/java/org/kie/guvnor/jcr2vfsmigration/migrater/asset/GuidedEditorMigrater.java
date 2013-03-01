package org.kie.guvnor.jcr2vfsmigration.migrater.asset;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.rpc.Asset;
import org.drools.guvnor.client.rpc.Module;
import org.drools.guvnor.client.rpc.RuleContentText;
import org.drools.guvnor.server.RepositoryAssetService;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.base.options.CommentedOption;
import org.kie.commons.java.nio.file.NoSuchFileException;
import org.kie.guvnor.guided.rule.service.GuidedRuleEditorService;
import org.kie.guvnor.jcr2vfsmigration.migrater.util.MigrationPathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;

@ApplicationScoped
public class GuidedEditorMigrater {

    protected static final Logger logger = LoggerFactory.getLogger(GuidedEditorMigrater.class);

    @Inject
    protected RepositoryAssetService jcrRepositoryAssetService;

    @Inject
    protected GuidedRuleEditorService guidedRuleEditorService;

    @Inject
    protected MigrationPathManager migrationPathManager;
    
    @Inject
    private Paths paths;
    
    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    public void migrate(Module jcrModule, Asset jcrAsset, final String checkinComment, final Date lastModified, String lastContributor) {
        if (!AssetFormats.BUSINESS_RULE.equals(jcrAsset.getFormat())) {
            throw new IllegalArgumentException("The jcrAsset (" + jcrAsset
                    + ") has the wrong format (" + jcrAsset.getFormat() + ").");
        }
        Path path = migrationPathManager.generatePathForAsset(jcrModule, jcrAsset);
        String sourceDRL = getSourceDRL((org.drools.ide.common.client.modeldriven.brl.RuleModel) jcrAsset.getContent());
/*        RuleModel vfsRuleModel = convertRuleModel(
                (org.drools.ide.common.client.modeldriven.brl.RuleModel) jcrAsset.getContent());*/
        //guidedRuleEditorService.save(path, vfsRuleModel, checkinComment);
        
        final org.kie.commons.java.nio.file.Path nioPath = paths.convert( path );

        Map<String, Object> attrs;

        try {
            attrs = ioService.readAttributes( nioPath );
        } catch ( final NoSuchFileException ex ) {
            attrs = new HashMap<String, Object>();
        }

        ioService.write( nioPath, sourceDRL, attrs, new CommentedOption(lastContributor, null, checkinComment, lastModified ));

    }

    private String getSourceDRL(org.drools.ide.common.client.modeldriven.brl.RuleModel model/*, BRMSPackageBuilder builder*/) {

        String drl = getBrlDrlPersistence().marshal( model );
/*        if ( builder.hasDSL() && model.hasDSLSentences() ) {
            drl = builder.getDSLExpander().expand( drl );
        }*/
        return drl;
    }

    protected org.drools.ide.common.server.util.BRLPersistence getBrlDrlPersistence() {
        return org.drools.ide.common.server.util.BRDRLPersistence.getInstance();
    }

}
