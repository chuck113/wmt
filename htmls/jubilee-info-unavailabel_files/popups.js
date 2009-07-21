$.fn.PopUpWindow = function(config){
        return this.each(function(){
                // configuration
                this.myConfig =
                {
                    // valid address you want to open
                        address: config && config.address ? config.address : null,
                        // name for the new window. Used when refering to the new windows later
                        name: config && config.name ? config.name : "PopUpWindow",
                        // additional parameters for the window configuration like showing the toolbar or scrollbars
                        params: config && config.params ? config.params : null
                }
                // for "A" tags. if they didn't provide an address for us, then we will
                // try to grab the link for the tag and use that
                if(this.nodeName.toLowerCase() == "a")
                {
                  // see if they gave us an address
                  if(!this.myConfig.address)
                  {
                    // try to grab the href link and use that
            		this.myConfig.address = this.href;
                  }
                }
				// append help text to the link title attribute
				$(this).attr("title", function()
				{
						return this.title + " (Opens in new window)"
				});
                // add the popup code to the tag's click event
                $(this).click(function()
                {
                        // build parameters
                        var params = "";
                        if(this.myConfig.params != null)
                        {
                                params += this.myConfig.params;
                        }
                        window.open(this.myConfig.address, this.myConfig.name, params).focus();
						return false; 
                });
				
        });
}

$(function(){
        $(".pop-up").PopUpWindow({
		  name:"microsite",
          params:"width=778, height=550, scrollbars=yes, resizable=yes"
        });
		
        $(".microsite").PopUpWindow({
		  name:"microsite",
          params:"width=778, height=550, scrollbars=yes, resizable=yes, location=yes, toolbar=yes"
        });
        
        $(".new-window").PopUpWindow({
          name:"_blank",
		  params:"scrollbars=yes, location=yes, status=yes, titlebar=yes, toolbar=yes, resizable=yes"
        });
		
		//if(externalSiteInNewWindow){
			$(".external-site").PopUpWindow({
			  name:"_blank",
			  params:"scrollbars=yes, location=yes, status=yes, titlebar=yes, toolbar=yes, resizable=yes"
			});
		//}
});


