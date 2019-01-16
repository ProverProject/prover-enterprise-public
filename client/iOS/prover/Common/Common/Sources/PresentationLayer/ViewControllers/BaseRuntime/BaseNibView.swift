//
//  BaseNibView.swift
//  Common
//
//  Created by Александр Соломатов on 23/11/2018.
//  Copyright © 2018 Timur Elzhov. All rights reserved.
//

import UIKit

class BaseNibView: UIView {
    
    var contentView: UIView!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        // Setup view from .xib file
        xibSetup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        // Setup view from .xib file
        xibSetup()
    }
    
    func commonSetup() { }
}

private extension BaseNibView {
    
    func xibSetup() {
        contentView = loadNib()
        // use bounds not frame or it'll be offset
        contentView.frame = bounds
        // Adding custom subview on top of our view
        addSubview(contentView)
        
        contentView.translatesAutoresizingMaskIntoConstraints = false
        
        contentView.leadingAnchor.constraint(equalTo: self.leadingAnchor).isActive = true
        contentView.trailingAnchor.constraint(equalTo: self.trailingAnchor).isActive = true
        contentView.topAnchor.constraint(equalTo: self.topAnchor).isActive = true
        contentView.bottomAnchor.constraint(equalTo: self.bottomAnchor).isActive = true
        
        self.commonSetup()
    }
}
