BeanDefinition = [
    // class of bean (used by hibernate for ORM)
  	"bean.class"     :      "org.mueller.booklibrary.Format",  
  	
  	// property list for field display order, and other necessary info
  	"prop1"          :  	"id",
      "prop1.id"     :        "generated",         // specify this is a key, how key works
      "prop1.label"  :        "Format ID",         // override default label of "keypath"
  	"prop2"			 :      "extension",
  	  "prop2.label"  :        "File Extension",
  	"prop3"			 :		"description",
  	  "prop3.widget" :        "bigtext"
      
];
  	