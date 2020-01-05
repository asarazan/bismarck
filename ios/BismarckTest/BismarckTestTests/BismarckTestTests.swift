//
//  BismarckTestTests.swift
//  BismarckTestTests
//
//  Created by Aaron Sarazan on 1/5/20.
//  Copyright © 2020 Aaron Sarazan. All rights reserved.
//

import XCTest
import bismarck

@testable import BismarckTest

class BismarckTestTests: XCTestCase {

    override func setUp() {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }
    
    func testBismarck() {
        let bis = BaseBismarck<NSString>()
        assert(bis.peek() == nil)
    }

    func testExample() {
        // This is an example of a functional test case.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
    }

    func testPerformanceExample() {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
        }
    }

}
