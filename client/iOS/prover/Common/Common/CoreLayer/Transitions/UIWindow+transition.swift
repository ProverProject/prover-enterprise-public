//
//  UIWindowTransitions.swift
//  Daniele Margutti
//
//  Created by Daniele Margutti.
//  Copyright © 2017 Daniele Margutti. All rights reserved.
//
import Foundation
import UIKit

public extension UIWindow {
    
    /// Transition Options
    public struct TransitionOptions {
        
        /// Direction of the animation
        ///
        /// - fade: fade to new controller
        /// - toTop: slide from bottom to top
        /// - toBottom: slide from top to bottom
        /// - toLeft: pop to left
        /// - toRight: push to right
        public enum Direction {
            case fade
            case toTop
            case toBottom
            case toLeft
            case toRight
            
            /// Return the associated transition
            ///
            /// - Returns: transition
            internal func transition() -> CATransition {
                let transition = CATransition()
                transition.type = .push
                switch self {
                case .fade:
                    transition.type = .fade
                    transition.subtype = nil
                case .toLeft:
                    transition.subtype = .fromLeft
                case .toRight:
                    transition.subtype = .fromRight
                case .toTop:
                    transition.subtype = .fromTop
                case .toBottom:
                    transition.subtype = .fromBottom
                }
                return transition
            }
        }
        
        /// Background of the transition
        ///
        /// - solidColor: solid color
        /// - customView: custom view
        public enum Background {
            case solidColor(_: UIColor)
            case customView(_: UIView)
        }
        
        /// Duration of the animation (default is 0.20s)
        public var duration: TimeInterval = 0.20
        
        /// Direction of the transition (default is `toRight`)
        public var direction: TransitionOptions.Direction = .toRight
        
        /// Style of the transition (default is `linear`)
        public var function: CAMediaTimingFunction = CAMediaTimingFunction(name: .linear)

        /// Background of the transition (default is `nil`)
        public var background: TransitionOptions.Background?
        
        /// Initialize a new options object with given direction and curve
        ///
        /// - Parameters:
        ///   - direction: direction
        ///   - style: style
        public init(direction: TransitionOptions.Direction = .toRight, style: CAMediaTimingFunctionName = .linear) {
            self.direction = direction
            self.function = CAMediaTimingFunction(name: style)
        }
        
        public init() { }
        
        /// Return the animation to perform for given options object
        internal var animation: CATransition {
            let transition = self.direction.transition()
            transition.duration = self.duration
            transition.timingFunction = self.function
            return transition
        }
    }
    
    
    /// Change the root view controller of the window
    ///
    /// - Parameters:
    ///   - controller: controller to set
    ///   - options: options of the transition
    public func setRootViewController(_ controller: UIViewController, options: TransitionOptions = TransitionOptions()) {

        var transitionWnd: UIWindow! = nil

        if let background = options.background {
            transitionWnd = UIWindow(frame: UIScreen.main.bounds)

            switch background {
            case .customView(let view):
                transitionWnd.rootViewController = UIViewController.newController(withView: view, frame: transitionWnd.bounds)
            case .solidColor(let color):
                transitionWnd.backgroundColor = color
            }

            transitionWnd.makeKeyAndVisible()
        }
        
        // Make animation
        layer.add(options.animation, forKey: kCATransition)
        rootViewController = controller
        makeKeyAndVisible()
        
        if let wnd = transitionWnd {
            DispatchQueue.main.asyncAfter(deadline: .now() + 1 + options.duration) {
                wnd.removeFromSuperview()
            }
        }
    }
}
