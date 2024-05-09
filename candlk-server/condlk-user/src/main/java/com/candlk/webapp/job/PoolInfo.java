package com.candlk.webapp.job;

import java.math.BigInteger;
import java.util.List;

import com.candlk.common.util.BeanUtil;
import lombok.Getter;
import lombok.Setter;
import org.web3j.abi.*;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.utils.Numeric;

import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

@Getter
@Setter
public class PoolInfo extends StaticStruct {

	public String poolAddress;
	public String owner;
	public String keyBucketTracker;
	public String esXaiBucketTracker;
	public BigInteger keyCount;
	public BigInteger totalStakedAmount;
	public BigInteger updateSharesTimestamp;
	public BigInteger ownerShare;
	public BigInteger keyBucketShare;
	public BigInteger stakedBucketShare;

	public BigInteger v1;
	public BigInteger v2;
	public BigInteger v3;
	public BigInteger v4;

	public BigInteger v5;
	public BigInteger _ownerStakedKeys;
	public BigInteger _ownerRequestedUnstakeKeyAmount;
	public BigInteger _ownerLatestUnstakeRequestLockTime;

	public BigInteger v6;
	public String _name;

	public PoolInfoVO toVO() {
		return BeanUtil.copy(this, PoolInfoVO::new);
	}

	public PoolInfo(Address poolAddress, Address owner, Address keyBucketTracker, Address esXaiBucketTracker,
	                Uint256 keyCount, Uint256 totalStakedAmount, Uint256 updateSharesTimestamp, Uint32 ownerShare,
	                Uint32 keyBucketShare, Uint32 stakedBucketShare, Uint256 v1, Uint256 v2, Uint256 v3, Uint256 v4, Uint256 v5,
	                Uint256 _ownerStakedKeys, Uint256 _ownerRequestedUnstakeKeyAmount, Uint256 _ownerLatestUnstakeRequestLockTime, Uint256 v6, Uint256 _name) {
		super(poolAddress, owner, keyBucketTracker, esXaiBucketTracker,
				keyCount, totalStakedAmount, updateSharesTimestamp, ownerShare,
				keyBucketShare, stakedBucketShare, v1, v2, v3, v4, v5,
				_ownerStakedKeys, _ownerRequestedUnstakeKeyAmount, _ownerLatestUnstakeRequestLockTime, v6, _name);
		this.poolAddress = poolAddress.getValue();
		this.owner = owner.getValue();
		this.keyBucketTracker = keyBucketTracker.getValue();
		this.esXaiBucketTracker = esXaiBucketTracker.getValue();
		this.keyCount = keyCount.getValue();
		this.totalStakedAmount = totalStakedAmount.getValue();
		this.updateSharesTimestamp = updateSharesTimestamp.getValue();
		this.ownerShare = ownerShare.getValue();
		this.keyBucketShare = keyBucketShare.getValue();
		this.stakedBucketShare = stakedBucketShare.getValue();
		this.v1 = v1.getValue();
		this.v2 = v2.getValue();
		this.v3 = v3.getValue();
		this.v4 = v4.getValue();
		this.v5 = v5.getValue();
		this._ownerStakedKeys = _ownerStakedKeys.getValue();
		this._ownerRequestedUnstakeKeyAmount = _ownerRequestedUnstakeKeyAmount.getValue();
		this._ownerLatestUnstakeRequestLockTime = _ownerLatestUnstakeRequestLockTime.getValue();
		this.v6 = v6.getValue();
		this._name = new String(new DynamicBytes(Numeric.hexStringToByteArray(_name.getValue().toString(16))).getValue()).replaceAll("\\u0000", "");

	}

	public static PoolInfo getPoolInfo(Web3j web3j, String contractAddress) throws Exception {
		final List<TypeReference<?>> outputParameters = List.of(new TypeReference<PoolInfo>() {
		});
		final Function function = new Function("getPoolInfo", List.of(), outputParameters);
		final EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, contractAddress, FunctionEncoder.encode(function)), LATEST).send();
		final String value = ethCall.getValue();
		final List<Type> output = FunctionReturnDecoder.decode(value, function.getOutputParameters());
		return (PoolInfo) output.get(0);
	}

	public static BigInteger getPoolsCount(Web3j web3j, String contractAddress) throws Exception {
		final Function function = new Function("getPoolsCount", List.of(), List.of(new TypeReference<Uint256>() {
		}));
		final EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(
				null, contractAddress, FunctionEncoder.encode(function)), LATEST).send();
		return ((Uint256) FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()).get(0)).getValue();
	}

	public static String getPoolAddress(Web3j web3j, String contractAddress, BigInteger index) throws Exception {
		final Function function = new Function("getPoolAddress", List.of(new Uint256(index)), List.of(new TypeReference<Address>() {
		}));
		final EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(null, contractAddress, FunctionEncoder.encode(function)), LATEST).send();
		return ((Address) FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()).get(0)).getValue();
	}

	public static String getDelegateOwner(Web3j web3j, String contractAddress) throws Exception {
		final Function function = new Function("delegateOwner", List.of(), List.of(new TypeReference<Address>() {
		}));
		final EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(
				null, contractAddress, FunctionEncoder.encode(function)), LATEST).send();
		return ((Address) FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters()).get(0)).getValue();
	}

}