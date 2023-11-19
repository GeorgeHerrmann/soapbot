The Coin Factory
    - Each minute, the coin factory will process coins based on the current upgrades
    - Players start with no upgrades and coins are not farmed.
    - In order to start using the coin factory, an initial "factory" must be purchased
        - Then, the factory can be upgraded
        - To prevent people who have no idea what the Factory is from just randomly getting coins

    - There will be upgrade paths, certain upgrades can only be gotten going down that upgrade path.
    - Upgrades will be FactoryUpgrade objects, which will define whether or not an upgrade can be purchased (boolean logic), what it does, etc
    - All upgrades strictly affect how many coins the factory produces each minute.
    - Coin factory farm amounts are based on an initial value, which can be added, subtracted, divided and multiplied to
        - FactoryUpgrades will influence each of these factors.
            - For example, one upgrade might double 
    
    - A CoinFarm Wizard will be the primary way users can interact with their CoinFactory
        - Can be used to buy/sell upgrades
            - Upgrades can be sold for half their purchase price
            - Upgrades will be based on the total coin value for a Guild
        - The Wizard DOES NOT need to be open for the CoinFactory to be farming coins
            - This will happen in the background, every minute (maybe change)