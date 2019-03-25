import Foundation
import Moya

public enum NetworkAPI {
    
    case get_status
    case get_balance
    
    case estimate_submit_message_fee(message: String)
    case estimate_request_swype_code_fee
    case estimate_submit_media_hash_for_normal_swype_fee
    case estimate_submit_media_hash_for_fast_swype_fee
    
    case submit_message(message: String)
    case request_swype_code
    case fast_request_swype_code
    
    case confirm_message_submission(txHash: String)
    case download_swype_code(txHash: String)
    
    case submit_media_hash_for_normal_swype_request(mediaHash: String, referenceTxHash: String)
    case submit_media_hash_for_fast_swype_request(mediaHash: String, referenceBlockHeight: UInt64)
    
    case confirm_media_hash_submission(txHash: String)
}

extension NetworkAPI: TargetType {
    
    private var mediaHashType: MediaHashType {
        return .sha256
    }
    
    private var dummyMediaHash: String {
        switch mediaHashType {
        case .sha256:
            return String(repeating: "00", count: 32)
        }
    }
    
    private var dummyReferenceTxHash: String {
        return String(repeating: "00", count: 32)
    }
    
    private var dummyReferenceBlockHeight: UInt64 {
        return 0
    }
    
    private var dummyClientID: UInt {
        return 100
    }
    
    public var baseURL: URL {
        return URL(string: Settings.baseURL!)!
    }
    
    private var version: String {
        return "v1"
    }
    
    public var path: String {
        
        let res: String
        
        switch self {
        case .get_status:
            res = "/ent/\(version)/get-status"
            
        case .get_balance:
            res = "/ent/\(version)/get-balance"
            
        case .estimate_submit_message_fee,
             .estimate_request_swype_code_fee,
             .estimate_submit_media_hash_for_normal_swype_fee,
             .estimate_submit_media_hash_for_fast_swype_fee:
            res = "/ent/\(version)/estimate-fee"
            
        case .submit_message,
             .confirm_message_submission:
            res = "/ent/\(version)/submit-message"
            
        case .request_swype_code,
             .download_swype_code:
            res = "/ent/\(version)/request-swype-code"
            
        case .fast_request_swype_code:
            res = "/ent/\(version)/fast-request-swype-code"
            
        case .submit_media_hash_for_normal_swype_request,
             .submit_media_hash_for_fast_swype_request,
             .confirm_media_hash_submission:
            res = "/ent/\(version)/submit-media-hash"
        }
        
        return res
    }
    
    public var method: Moya.Method {
        
        switch self {
        default:
            return .post
        }
    }
    
    public var sampleData: Data {
        return Data()
    }
    
    public var headers: [String: String]? {
        
        switch self {
        default:
            return ["Content-Type": "application/x-www-form-urlencoded",
                    "Accept": "application/json"]
        }
    }
    
    public var task: Task {
        
        let dict: [String: Any]
        
        switch self {
        case .get_status,
             .get_balance:
            dict = [:]
            
        case .estimate_submit_message_fee(let message):
            dict = ["request": EstimateFeeRequestType.submitMessage.rawValue,
                    "clientid": dummyClientID,
                    "message": message]
            
        case .estimate_request_swype_code_fee:
            dict = ["request": EstimateFeeRequestType.requestSwypeCode.rawValue]
            
        case .estimate_submit_media_hash_for_normal_swype_fee:
            dict = ["request": EstimateFeeRequestType.submitMediaHash.rawValue,
                    "mediahash": dummyMediaHash,
                    "mediahashtype": mediaHashType.rawValue,
                    "clientid": dummyClientID,
                    "referencetxhash": dummyReferenceTxHash]
            
        case .estimate_submit_media_hash_for_fast_swype_fee:
            dict = ["request": EstimateFeeRequestType.submitMediaHash.rawValue,
                    "mediahash": dummyMediaHash,
                    "mediahashtype": mediaHashType.rawValue,
                    "clientid": dummyClientID,
                    "referenceblockheight": dummyReferenceBlockHeight]
            
        case .request_swype_code,
             .fast_request_swype_code:
            dict = [:]
            
        case .submit_message(let message):
            dict = ["clientid": dummyClientID,
                    "message": message]
            
        case .confirm_message_submission(let txHash),
             .confirm_media_hash_submission(let txHash):
            dict = ["txhash": txHash]
            
        case .download_swype_code(let txHash):
            dict = ["txhash": txHash]
            
        case .submit_media_hash_for_normal_swype_request(let mediaHash, let referenceTxHash):
            dict = ["request": EstimateFeeRequestType.submitMediaHash.rawValue,
                    "mediahash": mediaHash,
                    "mediahashtype": mediaHashType.rawValue,
                    "clientid": dummyClientID,
                    "referencetxhash": referenceTxHash]
            
        case .submit_media_hash_for_fast_swype_request(let mediaHash, let referenceBlockHeight):
            dict = ["request": EstimateFeeRequestType.submitMediaHash.rawValue,
                    "mediahash": mediaHash,
                    "mediahashtype": mediaHashType.rawValue,
                    "clientid": dummyClientID,
                    "referenceblockheight": referenceBlockHeight]
        }
        
        let paramsString = dict.map { k, v in "\(k)=\(v)" }
            .joined(separator: "&")
        
        print("[NetworkAPI     ] ********* curl \(baseURL)\(path) -d \"\(paramsString)\" | jj *********************** !!")
        
        return .requestParameters(parameters: dict, encoding: URLEncoding.default)
    }
}
