if (!ORYX.Plugins) 
    ORYX.Plugins = {};

if (!ORYX.Config)
	ORYX.Config = {};

ORYX.Plugins.InlineTaskFormEditor = Clazz.extend({
	sourceMode: undefined,
	taskformeditor: undefined,
	taskformsourceeditor: undefined,
	taskformcolorsourceeditor: undefined,
	dialog: undefined,
	
	construct: function(facade){
		this.facade = facade;
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_TASKFORM_EDIT, this.showTaskFormEditor.bind(this));
		this.sourceMode = false;
	},
	showTaskFormEditor: function(options) {
		if(options && options.tn) {
			Ext.Ajax.request({
	            url: ORYX.PATH + 'taskformseditor',
	            method: 'POST',
	            success: function(response) {
	    	   		try {
	    	   			this._buildandshow(options.tn, response.responseText);
	    	   		} catch(e) {
	    	   			Ext.Msg.alert('Error initiating Form Editor :\n' + e);
	    	   		}
	            }.bind(this),
	            failure: function(){
	            	Ext.Msg.alert('Error initiating Form Editor.');
	            },
	            params: {
	            	action: 'load',
	            	taskname: options.tn,
	            	profile: ORYX.PROFILE,
	            	uuid : ORYX.UUID
	            }
	        });
		} else {
			Ext.Msg.alert('Task Name not specified.');
		}
	},
	_buildandshow: function(tn, defaultsrc) {
		var formvalue = "";
		if(defaultsrc && defaultsrc != "false") {
			formvalue = defaultsrc;
		}
		
		this.sourceMode = false;
		
		this.taskformeditor = new Ext.form.HtmlEditor({
			 id: Ext.id(),
	         value:     formvalue,
	     	 enableSourceEdit: false,
	         autoScroll: true
	       });
		
		this.dialog = new Ext.Window({
			id          : 'maineditorwindow',
			layout		: 'fit',
			autoCreate	: true, 
			title		: 'Editing Task Form: ' + tn , 
			height		: 570, 
			width		: 700, 
			modal		: true,
			collapsible	: false,
			fixedcenter	: true, 
			shadow		: true, 
			resizable   : true,
			proxyDrag	: true,
			keys:[{
				key	: 27,
				fn	: function(){
						this.dialog.hide()
				}.bind(this)
			}],
			items		:[this.taskformeditor],
			listeners	:{
				hide: function(){
					this.dialog.destroy();
				}.bind(this)				
			},
			buttons		: [{
                text: 'Save',
                handler: function(){
                    var saveLoadMask = new Ext.LoadMask(Ext.getBody(), {msg:'Storing Task Form'});
                    saveLoadMask.show();
                    var tosaveValue = "";
                    if(this.sourceMode) {
                    	tosaveValue = this.taskformcolorsourceeditor.getValue();
                    } else {
                    	tosaveValue = this.taskformeditor.getValue();
                    }
                    
                    Ext.Ajax.request({
        	            url: ORYX.PATH + 'taskformseditor',
        	            method: 'POST',
        	            success: function(request) {
        	    	   		try {
        	    	   			saveLoadMask.hide();
        	    	   			this.dialog.hide();
        	    	   		} catch(e) {
        	    	   			Ext.Msg.alert('Error saving Task Form:\n' + e);
        	    	   		}
        	            }.createDelegate(this),
        	            failure: function(){
        	            	Ext.Msg.alert('Error saving Task Form');
        	            },
        	            params: {
        	            	action: 'save',
        	            	taskname: tn,
        	            	profile: ORYX.PROFILE,
        	            	uuid : ORYX.UUID,
        	            	tfvalue: tosaveValue
        	            }
        	        });
                }.bind(this)
            }, {
                text: 'Cancel',
                handler: function(){
                	this.dialog.hide()
                }.bind(this)
            }],
            tbar: [
                     {
                	   text: 'Switch Mode',
                	   handler : function() {
                	      if(this.sourceMode) {
                	    	  var editorValue = "";
                	    	  if(this.taskformcolorsourceeditor) {
                	    		  editorValue = this.taskformcolorsourceeditor.getValue();
                	    	  } else {
                	    		  this.taskformsourceeditor.getValue();
                	    	  }
                	    	  this.taskformeditor = new Ext.form.HtmlEditor({
                	 			 id: Ext.id(),
                	 	         value: editorValue,
                	 	     	 enableSourceEdit: false,
                	 	         autoScroll: true
                	 	       });
                	    	  this.dialog.remove(this.taskformsourceeditor, true);
                	    	  this.dialog.add(this.taskformeditor);
                	    	  this.dialog.doLayout();
                	    	  this.sourceMode = !this.sourceMode;
                	      } else {
                	    	  var sourceeditorid = Ext.id();
                	    	  this.taskformsourceeditor = new Ext.form.TextArea({
                	  			id: sourceeditorid,
                	  			anchor: '100%',
                	  	        autoScroll: true,
                	  	        value:this.taskformeditor.getValue()
                	  	      });
                	    	  this.dialog.remove(this.taskformeditor, true);
                	    	  this.dialog.add(this.taskformsourceeditor);
                	    	  this.dialog.doLayout();
                	    	  this.sourceMode = !this.sourceMode;
                	    	  this.taskformcolorsourceeditor = CodeMirror.fromTextArea(document.getElementById(sourceeditorid), {
                	 			  mode: "text/html",
                	 			  lineNumbers: true,
                	 			  lineWrapping: true,
                	 			  extraKeys: {
                	 				"'>'": function(cm) { cm.closeTag(cm, '>'); },
                	 				"'/'": function(cm) { cm.closeTag(cm, '/'); }
                	 			  }
                	 			});
                	      }
                	   }.bind(this)
                     }
            ]
		});		
		this.dialog.show();
	}
});