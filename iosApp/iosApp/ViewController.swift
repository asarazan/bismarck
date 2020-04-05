import UIKit
import app

class ViewController: UIViewController {
    @IBOutlet weak var label: UILabel!
    
    let vm = FooViewModel()
        
    override func viewWillAppear(_ animated: Bool) {
        vm.foo.onValue {
            it in
            self.label.text = "Hello and \(it ?? "null")"
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}
